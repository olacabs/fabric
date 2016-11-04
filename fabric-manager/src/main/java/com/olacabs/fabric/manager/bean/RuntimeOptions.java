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

package com.olacabs.fabric.manager.bean;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * Runtime options bean.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuntimeOptions {

    @Getter
    @Setter
    private String jvmOpts = "";

    @Getter
    @Setter
    private List<String> uris = Collections.singletonList("file:///root/.dockercfg");

    @Getter
    @Setter
    private String logLevel = "INFO";

    @Getter
    @Setter
    private boolean metricsDisabled = false;

    @Getter
    @Setter
    @JsonProperty("docker")
    private DockerConfig dockerConfig = new DockerConfig();

    @Getter
    @Setter
    private String executorDockerImage = "";

    @Getter
    @Setter
    private double topologyCpu = 0.0;

    @Getter
    @Setter
    private double topologyMemory = 0.0;

    @Getter
    @Setter
    private int instances = 1;
}
