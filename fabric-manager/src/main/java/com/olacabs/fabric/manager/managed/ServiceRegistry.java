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

package com.olacabs.fabric.manager.managed;

import java.net.InetAddress;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.ServiceProviderBuilders;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.serviceprovider.ServiceProvider;
import com.google.common.base.Strings;
import com.olacabs.fabric.manager.bean.ShardInfo;
import com.olacabs.fabric.manager.config.ServiceDiscoveryConfig;

import io.dropwizard.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.server.SimpleServerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Todo .
 */
@Slf4j
public class ServiceRegistry implements Managed {

    private final ObjectMapper mapper;
    private final ServiceDiscoveryConfig registryConfig;
    private final String host;
    private final int port;
    private ServiceProvider<ShardInfo> serviceProvider;

    public ServiceRegistry(final ObjectMapper objectMapper, final Configuration dropwizardConfiguration,
            final ServiceDiscoveryConfig registryConfig) throws Exception {
        this.mapper = objectMapper;
        this.registryConfig = registryConfig;
        this.host = Strings.isNullOrEmpty(registryConfig.getHostname()) ? getHost() : registryConfig.getHostname();
        this.port = 0 >= registryConfig.getPort() ? getPort(dropwizardConfiguration) : registryConfig.getPort();
    }

    @Override
    public void start() throws Exception {
        // Register
        this.serviceProvider = ServiceProviderBuilders.<ShardInfo>shardedServiceProviderBuilder()
                .withConnectionString(registryConfig.getZkConnectionString())
                .withNamespace(registryConfig.getNamespace()).withServiceName(registryConfig.getServiceName())
                .withSerializer(data -> {
                    try {
                        return mapper.writeValueAsBytes(data);
                    } catch (final JsonProcessingException ignore) {
                        log.error("Error - ", ignore);
                    }
                    return null;
                }).withHostname(host).withPort(port).withNodeData(new ShardInfo(registryConfig.getEnvironment()))
                .withHealthcheck(() -> HealthcheckStatus.healthy).buildServiceDiscovery();

        // Start
        this.serviceProvider.start();
        log.info(
                "Service registered to ranger with namespace - '{}', service - '{}', "
                        + "host - '{}', port - '{}', environment - '{}'",
                registryConfig.getNamespace(), registryConfig.getServiceName(), host, port,
                registryConfig.getEnvironment());
    }

    @Override
    public void stop() throws Exception {
        // Stop
        this.serviceProvider.stop();
        log.info("Service registry stopped...");
    }

    /*
     * @return host
     */
    private String getHost() throws Exception {
        return InetAddress.getLocalHost().getHostAddress();
    }

    /*
     * @return port
     */
    private int getPort(Configuration config) {
        int httpPort = 0;
        final SimpleServerFactory serverFactory = (SimpleServerFactory) config.getServerFactory();
        final HttpConnectorFactory connector = (HttpConnectorFactory) serverFactory.getConnector();
        if (connector.getClass().isAssignableFrom(HttpConnectorFactory.class)) {
            httpPort = connector.getPort();
        }
        return httpPort;
    }
}
