/*
 * Copyright 2016 ANI Technologies Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olacabs.fabric.manager.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.olacabs.fabric.manager.bean.ExecutorConfig;
import com.olacabs.fabric.manager.bean.RuntimeOptions;
import com.olacabs.fabric.manager.config.ManagerConfig;
import com.olacabs.fabric.manager.dao.IApplicationDAO;
import com.olacabs.fabric.manager.domain.ApplicationDomain;
import com.olacabs.fabric.manager.domain.ComponentInstanceDomain;
import com.olacabs.fabric.manager.domain.ComputationDomain;
import com.olacabs.fabric.manager.exception.FabricManagerException;
import com.olacabs.fabric.manager.service.IApplicationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.Parameter;
import mesosphere.marathon.client.model.v2.UpgradeStrategy;
import mesosphere.marathon.client.utils.MarathonException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Todo .
 */
@Slf4j
@AllArgsConstructor
@Data
public class ApplicationService implements IApplicationService {

    private final IApplicationDAO dao;
    private final Marathon marathon;
    private final ManagerConfig managerConfig;

    @Override
    public String name(final String tenant, final String name) {
        return String.format("/fabric/%s/%s", tenant.toLowerCase().replaceAll("_", "-"), name.toLowerCase());
    }

    @Override
    public ApplicationDomain create(final ApplicationDomain application, final String tenant, final int instances) {

        final ComputationDomain computation = application.getComputation();
        final ExecutorConfig executorConfig = application.getExecutorConfig();
        final RuntimeOptions runtimeOpts = application.getRuntimeOptions();

        double cpu = allocateCpu(computation, runtimeOpts);
        double mem = allocateMemory(computation, runtimeOpts);
        cpu = Math.max(executorConfig.getMinCpu(), cpu);
        mem = Math.max(executorConfig.getMinMemory(), mem);

        final App app = App
                .dockerAppBuilder(name(tenant, computation.getName()), executorConfig.getExecutorDockerImage(), cpu,
                        mem, instances)
                .forcePullImage(true).network(runtimeOpts.getDockerConfig().getNetworkType().name()).privileged(false)
                .build();

        final List<Parameter> parameters = getParameters(computation, runtimeOpts, app);
        final String fabricManagerConnectionString = getFabricConnString();
        final String specUrl = String.format("http://%s/v1/computations/%s/%s?version=%d",
                fabricManagerConnectionString, tenant, computation.getName(), computation.getVersion());
        final Map<String, String> labels = getLabels(computation, executorConfig);
        final String openTsdbHost = getOpenTsdb();
        final double heapSize = managerConfig.getExecutor().getAlpha() * mem;
        final Map<String, String> env = setEnvVariables(computation, tenant, computation.getInternalId(), runtimeOpts,
                specUrl, openTsdbHost, heapSize);

        app.setEnv(env);
        app.setLabels(labels);
        app.setUpgradeStrategy(new UpgradeStrategy(1.0, 0.0));
        app.getContainer().setVolumes(runtimeOpts.getDockerConfig().getVolumes());
        app.getContainer().getDocker().setParameters(parameters);

        log.debug("Spawning computation: [{}:{}:{}] with {} instances using URL: {}", tenant, computation.getName(),
                Integer.toString(instances), computation.getInternalId(), specUrl);
        log.info("Creating app with configuration: {}", app.toString());

        // deploy and saveOrUpdate
        deploy(app);

        return persist(application, tenant);
    }

    @Override
    public void delete(final String appName, final String tenant) {
        final ApplicationDomain application = get(appName, tenant);
        dao.delete(application);
        try {
            marathon.deleteApp(appName);
        } catch (final MarathonException e) {
            log.error("Failed while deleting app from marathon", e);
            dao.revive(application);
            throw new FabricManagerException(e);
        }
    }

    @Override
    public ApplicationDomain get(final String appName, final String tenant) {
        final Set<ApplicationDomain> applications = dao.search(appName, tenant);
        if (applications.isEmpty()) {
            return null;
        }

        return applications.iterator().next();
    }

    @Override
    public Map getAppFromDeploymentEnv(final String appName) {
        final GetAppResponse response;
        try {
            response = marathon.getApp(appName);
        } catch (final MarathonException e) {
            log.error("Failed while getting app from marathon", e);
            throw new FabricManagerException(e);
        }
        return Collections.singletonMap("response", response);
    }

    @Override
    public void scale(final String appName, final String tenant, final int scaleTo) {
        get(appName, tenant);
        final GetAppResponse getAppResponse;
        try {
            getAppResponse = marathon.getApp(appName);
        } catch (final MarathonException e) {
            log.error("Failed while getting app from marathon", e);
            throw new FabricManagerException(e);
        }
        getAppResponse.getApp().setInstances(scaleTo);
        marathon.updateApp(appName, getAppResponse.getApp());
        update(appName, tenant, ApplicationDomain.builder()
                .instances(scaleTo)
                .build());
    }

    @Override
    public void update(final String appName, final String tenant, final ApplicationDomain updateParam) {
        final ApplicationDomain fetched = get(appName, tenant);
        fetched.update(updateParam);
        dao.modify(fetched);
    }

    @Override
    public void restart(final String appName, final String tenant, final boolean forcefully) {
        get(appName, tenant);
        marathon.restartApp(appName, forcefully);
        update(appName, tenant, ApplicationDomain.builder().build());
    }

    /*
     * Get params
     */
    private List<Parameter> getParameters(final ComputationDomain computation, final RuntimeOptions runtimeOpts,
                                          final App app) {
        final List<Parameter> parameters = Lists.newArrayList();
        parameters.addAll(runtimeOpts.getDockerConfig().getDockerOpts().getParameters());
        parameters.add(new Parameter("label", String.format("id=%s", computation.getName())));
        runtimeOpts.getUris().stream().forEach(app::addUri);
        return parameters;
    }

    /*
     * Get fabric connection string
     */
    private String getFabricConnString() {
        String fabricServerConnectionString = managerConfig.getFabricManagerConnectionString();
        if (Strings.isNullOrEmpty(fabricServerConnectionString)) {
            fabricServerConnectionString = "_" + com.olacabs.fabric.manager.App.APP_NAME + "_tcp.marathon.mesos.";
        }
        return fabricServerConnectionString;
    }

    /*
     * Get labels to set
     */
    private Map<String, String> getLabels(final ComputationDomain computation, final ExecutorConfig executorConfig) {
        final Map<String, String> labels = Maps.newHashMap();
        labels.put("computation-id", computation.getName());

        final String[] tokens = executorConfig.getExecutorDockerImage().split(":");
        labels.put("executor-version", (tokens.length > 2) ? tokens[2] : "latest");
        labels.put("version", "1");
        return labels;
    }

    /*
     * Get OpenTsdbHost
     */
    private String getOpenTsdb() {
        return managerConfig.getOpenTsdbConfig().getOpenTsdbUrl().replaceFirst("http://", "").replaceFirst(":[0-9]+$",
                "");
    }

    /*
     * Get environment variables
     */
    private Map<String, String> setEnvVariables(final ComputationDomain computation, final String tenant, final int id,
                                                final RuntimeOptions runtimeOpts, final String specUrl,
                                                final String openTsdbHost, final double heapSize) {
        final Map<String, String> env = Maps.newHashMap();
        env.put("HEAP_SIZE", String.format("%dm", (int) Math.rint(heapSize)));
        env.put("JVM_OPTS", runtimeOpts.getJvmOpts());
        env.put("OPENTSDB_URL", openTsdbHost);
        env.put("SPEC_LOCATION", specUrl);
        env.put("LOG_PREFIX", String.format("fabric-%s", String.format("%s-%s-%s", tenant, computation.getName(), id)));
        env.put("LOG_LEVEL", runtimeOpts.getLogLevel());
        env.put("METRICS_DISABLED", String.valueOf(runtimeOpts.isMetricsDisabled()));
        return env;
    }

    /*
     * create or scale
     */
    private void deploy(final App app) {
        GetAppResponse getAppResponse = null;
        boolean shouldUpdate = true;
        try {
            getAppResponse = marathon.getApp(app.getId());
        } catch (final MarathonException me) {
            log.info("App with id = {} doesn't exist, so doing a POST", app.getId(), me);
            try {
                marathon.createApp(app);
            } catch (final MarathonException e) {
                log.error("Creation of marathon app failed - ", e);
                throw new FabricManagerException(e);
            }
            shouldUpdate = false;
        }

        if (shouldUpdate) {
            log.info("App with id = {} already exists, so doing a PUT", app.getId());

            final Map<String, String> oldLabels = getAppResponse.getApp().getLabels();
            if (oldLabels.containsKey("version")) {
                int version = Integer.valueOf(oldLabels.get("version"));
                version++;
                oldLabels.put("version", "" + version);
            } else {
                oldLabels.put("version", "1");
            }
            app.setLabels(oldLabels);
            marathon.updateApp(app.getId(), app);
        }
    }

    private ApplicationDomain persist(final ApplicationDomain application, final String tenant) {
        final ApplicationDomain fetchedApp = get(application.getName(), tenant);
        if (fetchedApp == null) {
            application.setActive(true);
            return dao.save(application);
        } else {
            update(application.getName(), tenant, ApplicationDomain.builder()
                    .active(true)
                    .instances(application.getInstances())
                    .build());
        }
        return get(application.getName(), tenant);
    }

    private double allocateMemory(ComputationDomain computation, RuntimeOptions runtimeOpts) {
        double mem = 0.0;
        if (runtimeOpts.getTopologyMemory() > 0) {
            mem = runtimeOpts.getTopologyMemory();
        } else {
            for (final ComponentInstanceDomain source : computation.getSources()) {
                mem = mem + source.getComponent().getMemory();
            }
            for (final ComponentInstanceDomain processor : computation.getProcessors()) {
                mem = mem + processor.getComponent().getMemory();
            }
        }
        return mem;
    }

    private double allocateCpu(ComputationDomain computation, RuntimeOptions runtimeOpts) {
        double cpu = 0.0;
        if (runtimeOpts.getTopologyCpu() > 0) {
            cpu = runtimeOpts.getTopologyCpu();
        } else {
            for (final ComponentInstanceDomain source : computation.getSources()) {
                cpu = cpu + source.getComponent().getCpu();
            }
            for (final ComponentInstanceDomain processor : computation.getProcessors()) {
                cpu = cpu + processor.getComponent().getCpu();
            }
        }
        return cpu;
    }
}
