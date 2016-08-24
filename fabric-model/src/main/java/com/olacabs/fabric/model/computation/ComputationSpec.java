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

package com.olacabs.fabric.model.computation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * TODO Add more.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputationSpec {
    private Properties properties = new Properties();
    private String id;
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @Email
    private String email;
    @NotNull
    private String description;
    @Singular
    @NotNull
    private Map<String, String> attributes = Maps.newHashMap();
    @Singular
    @NotNull
    @NotEmpty
    private Set<ComponentInstance> sources;
    @Singular
    @NotNull
    @NotEmpty
    private Set<ComponentInstance> processors;
    @Singular
    @NotNull
    @NotEmpty
    private List<Connection> connections;
}
