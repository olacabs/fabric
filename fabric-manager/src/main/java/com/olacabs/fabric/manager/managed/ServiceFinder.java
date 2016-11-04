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

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.ServiceFinderBuilders;
import com.flipkart.ranger.finder.sharded.SimpleShardedServiceFinder;
import com.flipkart.ranger.model.ServiceNode;
import com.olacabs.fabric.manager.bean.ShardInfo;
import com.olacabs.fabric.manager.config.ServiceDiscoveryConfig;

import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;

/**
 * Todo .
 */
@Slf4j
public class ServiceFinder implements Managed {

    private final ObjectMapper mapper;
    private final ServiceDiscoveryConfig registryConfig;
    private SimpleShardedServiceFinder<ShardInfo> serviceFinder;

    public ServiceFinder(final ObjectMapper mapper, final ServiceDiscoveryConfig registryConfig) {
        this.mapper = mapper;
        this.registryConfig = registryConfig;
    }

    @Override
    public void start() throws Exception {
        this.serviceFinder = ServiceFinderBuilders.<ShardInfo>shardedFinderBuilder()
                .withConnectionString(registryConfig.getZkConnectionString())
                .withNamespace(registryConfig.getNamespace()).withServiceName(registryConfig.getServiceName())
                .withDeserializer(data -> {
                    try {
                        return mapper.readValue(data, new TypeReference<ServiceNode<ShardInfo>>() {});
                    } catch (final IOException ignore) {
                        log.error("Error - ", ignore);
                    }
                    return null;
                }).build();
        this.serviceFinder.start();
        log.info("Started service finder...");
    }

    @Override
    public void stop() throws Exception {
        this.serviceFinder.stop();
        log.info("Stopped service finder...");
    }

    public List<ServiceNode<ShardInfo>> getAllNodes() {
        return serviceFinder.getAll(new ShardInfo(registryConfig.getEnvironment()));
    }
}
