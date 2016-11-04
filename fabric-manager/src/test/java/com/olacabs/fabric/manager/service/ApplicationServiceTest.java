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

package com.olacabs.fabric.manager.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.olacabs.fabric.manager.bean.ExecutorConfig;
import com.olacabs.fabric.manager.bean.RuntimeOptions;
import com.olacabs.fabric.manager.config.ManagerConfig;
import com.olacabs.fabric.manager.config.OpenTsdbConfig;
import com.olacabs.fabric.manager.dao.IApplicationDAO;
import com.olacabs.fabric.manager.dao.impl.ApplicationDAO;
import com.olacabs.fabric.manager.domain.ApplicationDomain;
import com.olacabs.fabric.manager.domain.ComputationDomain;
import com.olacabs.fabric.manager.service.impl.ApplicationService;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.Result;
import mesosphere.marathon.client.utils.MarathonException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Application service test.
 */
public class ApplicationServiceTest {
    private static IApplicationDAO applicationDAO;
    private static Marathon marathon;
    private static ManagerConfig managerConfig;
    private static ApplicationService applicationService;
    private static ApplicationDomain application;
    private static ApplicationDomain updatedApplication;
    private static String tenant = "tenant";
    private static ComputationDomain computation;
    private static RuntimeOptions runtimeOptions;
    private static OpenTsdbConfig openTsdbConfig;
    private static ExecutorConfig executorConfig;
    private static ComputationDomain updatedComputation;
    private static GetAppResponse getAppResponse;

    @BeforeClass
    public static void setUp() throws Exception {
        applicationDAO = mock(ApplicationDAO.class);
        marathon = mock(Marathon.class);
        managerConfig = mock(ManagerConfig.class);

        getOpenTsdbConfig();
        getExecutorConfig();
        getComputation();
        getRunTimeOptions();
        getApplication();

        applicationService = new ApplicationService(applicationDAO, marathon, managerConfig);

        final Set<ApplicationDomain> applicationDomainSet = Sets.newHashSet();
        applicationDomainSet.add(application);
        doReturn(applicationDomainSet).when(applicationDAO).search(anyString(), anyString());
        doReturn(application).when(applicationDAO).save(any(ApplicationDomain.class));
        doNothing().when(applicationDAO).modify(any(ApplicationDomain.class));
        doNothing().when(applicationDAO).delete(any(ApplicationDomain.class));
        doNothing().when(marathon).updateApp(anyString(), any(App.class));
        doNothing().when(marathon).restartApp(anyString(), anyBoolean());
        doReturn(new Result()).when(marathon).deleteApp(anyString());

        final App app = new App();
        app.setInstances(1);
        final Map<String, String> labels = Maps.newHashMap(); labels.put("version", "1");
        app.setLabels(labels);
        getAppResponse = new GetAppResponse();
        getAppResponse.setApp(app);
        doReturn(getAppResponse).when(marathon).getApp(anyString());
    }

    private static void getExecutorConfig() {
        executorConfig = new ExecutorConfig();
        executorConfig.setExecutorDockerImage("localhost/executor:0.0.1");
    }

    private static void getOpenTsdbConfig() {
        openTsdbConfig = new OpenTsdbConfig();
        openTsdbConfig.setOpenTsdbUrl("http://localhost:4242");
    }

    private static void getRunTimeOptions() {
        runtimeOptions = new RuntimeOptions();
    }

    private static void getComputation() {
        computation = new ComputationDomain();
        computation.setName("test-computation");
        computation.setDescription("test-description");
        computation.setOwnerEmail("testemail@email.com");
        computation.setTenant(tenant);
        computation.setAttributes(Maps.newHashMap());
        computation.setSources(Sets.newHashSet());
        computation.setProcessors(Sets.newHashSet());
        computation.setConnections(Sets.newHashSet());
        computation.setProperties(Maps.newHashMap());

        updatedComputation = computation;
        updatedComputation.setDescription("test-updated-description");
        updatedComputation.setOwnerEmail("testupdatedemail@email.com");
    }

    private static void getApplication() {
        application = new ApplicationDomain();
        application.setName("test-application");
        application.setTenant(tenant);
        application.setComputation(computation);
        application.setExecutorConfig(executorConfig);
        application.setRuntimeOptions(runtimeOptions);

        updatedApplication = application;
        updatedApplication.setComputation(updatedComputation);
    }

    @Test
    public void testAppCreate() throws MarathonException {
        doThrow(new MarathonException(404, "Not Found")).doReturn(getAppResponse).when(marathon).getApp(anyString());
        doReturn(new App()).when(marathon).createApp(any(App.class));
        doReturn(openTsdbConfig).when(managerConfig).getOpenTsdbConfig();
        doReturn(executorConfig).when(managerConfig).getExecutor();

        applicationService.create(application, tenant, 1);
    }

    @Test
    public void testAppUpdate() throws MarathonException {
        doReturn(new App()).when(marathon).createApp(any(App.class));
        doReturn(openTsdbConfig).when(managerConfig).getOpenTsdbConfig();
        doReturn(executorConfig).when(managerConfig).getExecutor();

        applicationService.create(updatedApplication, tenant, 1);
    }

    @Test
    public void testAppGet() throws MarathonException {
        ApplicationDomain result = applicationService.get(application.getName(), tenant);
        Assert.assertEquals(result, application);
    }

    @Test
    public void testAppGetFromMarathon() throws MarathonException {
        Map result = applicationService.getAppFromDeploymentEnv(application.getName());
        Assert.assertTrue(result instanceof Map);
        Assert.assertTrue(result.get("response") instanceof GetAppResponse);
    }

    @Test
    public void testAppScale() throws MarathonException {
        applicationService.scale(application.getName(), tenant, 10);
    }

    @Test
    public void testAppRestart() throws MarathonException {
        applicationService.restart(application.getName(), tenant, true);
    }

    @Test
    public void testAppDelete() throws MarathonException {
        applicationService.delete(application.getName(), tenant);
    }
}
