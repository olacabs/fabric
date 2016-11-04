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

package com.olacabs.fabric.manager.config;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

/**
 * Registry configuration.
 */
@Data
public class ServiceDiscoveryConfig {

    @NotNull
    @NotEmpty
    private String zkConnectionString;

    /**
     * Service name to be used by clients for discovery.
     */
    @NotNull
    @NotEmpty
    private String serviceName;

    @NotNull
    @NotEmpty
    private String namespace = "fabric";

    /**
     * Set environment to dev/stage/qa/prod etc.
     */
    @NotNull
    @NotEmpty
    private String environment;

    /**
     * Using this is not recommended.
     * It is used by services that get deployed through docker/mesos etc.
     * This will default to the localhost name as derived from InetAdderss.getLocalHost().getCanonicalName()
     */
    private String hostname;

    /**
     * Using this is not recommended.
     * It is used by services that get deployed through docker/mesos etc.
     * This is read in by default from port settings in config.yml connector configuration.
     */
    private int port = -1;
}
