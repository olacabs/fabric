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

package com.olacabs.fabric.manager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.olacabs.fabric.compute.builder.impl.JarScanner;
import com.olacabs.fabric.manager.config.ManagerConfig;
import com.olacabs.fabric.manager.dao.IApplicationDAO;
import com.olacabs.fabric.manager.dao.IComponentDAO;
import com.olacabs.fabric.manager.dao.IComputationDAO;
import com.olacabs.fabric.manager.dao.IGlobalPropertyDAO;
import com.olacabs.fabric.manager.dao.impl.ApplicationDAO;
import com.olacabs.fabric.manager.dao.impl.ComponentDAO;
import com.olacabs.fabric.manager.dao.impl.ComputationDAO;
import com.olacabs.fabric.manager.dao.impl.GlobalPropertyDAO;
import com.olacabs.fabric.manager.domain.*;
import com.olacabs.fabric.manager.exception.mapper.HttpExceptionMapper;
import com.olacabs.fabric.manager.exception.mapper.RuntimeExceptionMapper;
import com.olacabs.fabric.manager.filter.UserFilter;
import com.olacabs.fabric.manager.managed.OpenTsdbMetricReporter;
import com.olacabs.fabric.manager.managed.ServiceFinder;
import com.olacabs.fabric.manager.managed.ServiceRegistry;
import com.olacabs.fabric.manager.resource.ApplicationResource;
import com.olacabs.fabric.manager.resource.ComponentResource;
import com.olacabs.fabric.manager.resource.*;
import com.olacabs.fabric.manager.service.IApplicationService;
import com.olacabs.fabric.manager.service.IComponentService;
import com.olacabs.fabric.manager.service.IComputationService;
import com.olacabs.fabric.manager.service.IGlobalPropertyService;
import com.olacabs.fabric.manager.service.impl.ApplicationService;
import com.olacabs.fabric.manager.service.impl.ComponentService;
import com.olacabs.fabric.manager.service.impl.ComputationService;
import com.olacabs.fabric.manager.service.impl.GlobalPropertyService;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import mesosphere.marathon.client.MarathonClient;
import minisu.dropwizard.interpolation.EnvironmentVariableInterpolationBundle;

/**
 * Todo .
 */
public class App extends Application<ManagerConfig> {

    public static final String APP_NAME = "fabric-manager";

    /*
     * Hibernate bundle
     */
    private final HibernateBundle<ManagerConfig> hibernate = new HibernateBundle<ManagerConfig>(ConnectionDomain.class,
            ComputationDomain.class, ComponentInstanceDomain.class, ComponentDomain.class, GlobalPropertyDomain.class,
            ApplicationDomain.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(final ManagerConfig configuration) {
            return configuration.getDatabase();
        }
    };

    /*
     * Swagger bundle
     */
    private final SwaggerBundle<ManagerConfig> swaggerBundle = new SwaggerBundle<ManagerConfig>() {
        @Override
        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ManagerConfig configuration) {
            return configuration.getSwaggerBundleConfiguration();
        }
    };

    @Override
    public void initialize(final Bootstrap<ManagerConfig> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.addBundle(new EnvironmentVariableInterpolationBundle());
        bootstrap.addBundle(hibernate);
        bootstrap.addBundle(swaggerBundle);
    }

    @Override
    public void run(final ManagerConfig config, final Environment environment) throws Exception {
        environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final IComponentDAO componentDAO = new ComponentDAO(hibernate.getSessionFactory());
        final IApplicationDAO applicationDAO = new ApplicationDAO(hibernate.getSessionFactory());
        final IGlobalPropertyDAO globalPropertyDAO = new GlobalPropertyDAO(hibernate.getSessionFactory());
        final IComputationDAO computationDAO = new ComputationDAO(hibernate.getSessionFactory());

        final IComponentService componentService = new ComponentService(componentDAO);
        final IComputationService computationService = new ComputationService(computationDAO, componentDAO);
        final IGlobalPropertyService globalPropertyService = new GlobalPropertyService(globalPropertyDAO);
        final IApplicationService applicationService = new ApplicationService(applicationDAO,
                MarathonClient.getInstance(config.getMarathonEndpoint()), config);

        final ComponentResource componentResource =
                new ComponentResource(componentService, config.getArtifactoryPath(), new JarScanner());
        final ComputationResource computationResource = new ComputationResource(computationService);
        final GlobalPropertyResource globalPropertyResource = new GlobalPropertyResource(globalPropertyService);

        final ApplicationResource appResource =
                new ApplicationResource(applicationService, computationService, config.getExecutor());
        final OpenTsdbMetricReporter openTsdb = new OpenTsdbMetricReporter(config, environment.metrics());

        final ServiceRegistry serviceRegistry =
                new ServiceRegistry(environment.getObjectMapper(), config, config.getServiceDiscovery());
        final ServiceFinder serviceFinder =
                new ServiceFinder(environment.getObjectMapper(), config.getServiceDiscovery());
        final ServiceRegistryResource serviceRegistryResource = new ServiceRegistryResource(serviceFinder);

        /**
         * Register managed, resources, exception mapper, filters
         */
        environment.lifecycle().manage(openTsdb);
        environment.lifecycle().manage(serviceRegistry);
        environment.lifecycle().manage(serviceFinder);

        environment.jersey().register(componentResource);
        environment.jersey().register(computationResource);
        environment.jersey().register(serviceRegistryResource);
        environment.jersey().register(appResource);
        environment.jersey().register(globalPropertyResource);

        environment.jersey().register(RuntimeExceptionMapper.class);
        environment.jersey().register(HttpExceptionMapper.class);

        environment.jersey().register(UserFilter.class);
    }

    /**
     * @param args to start java process
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final App app = new App();
        app.run(args);
    }
}
