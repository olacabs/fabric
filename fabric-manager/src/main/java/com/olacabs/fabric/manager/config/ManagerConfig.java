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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.olacabs.fabric.manager.bean.ExecutorConfig;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.*;

/**
 * Todo .
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerConfig extends Configuration {

    @Getter
    @Setter
    @JsonProperty("swagger")
    private SwaggerBundleConfiguration swaggerBundleConfiguration = new SwaggerBundleConfiguration();
    @NotNull
    @Getter
    private String artifactoryPath;
    @Valid
    @Getter
    private ServiceDiscoveryConfig serviceDiscovery = new ServiceDiscoveryConfig();
    @Valid
    @Getter
    @JsonProperty("opentsdb")
    private OpenTsdbConfig openTsdbConfig = new OpenTsdbConfig();
    @NotNull
    @Valid
    @Getter
    private ExecutorConfig executor = new ExecutorConfig();
    @NotNull
    @Getter
    private String marathonEndpoint;
    @Getter
    private String fabricManagerConnectionString;
    @NotNull
    @Valid
    @Getter
    private DataSourceFactory database = new DataSourceFactory();
}
