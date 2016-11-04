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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import com.olacabs.fabric.manager.domain.ComponentDomain;

import lombok.*;

/**
 * Component versions class bean.
 */
@Builder
@Data
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentVersions {

    @NotNull
    private String namespace;

    @NotNull
    private String name;

    @Singular
    private List<String> versions = Lists.newArrayList();

    /**
     * Resolves list of computation versions from meta .
     *
     * @param components to resolve
     * @return list of component versions
     */
    @JsonIgnore
    public static List<ComponentVersions> resolve(final Set<ComponentDomain> components) {
        final List<ComponentVersions> componentVersions = Lists.newArrayList();
        components.stream()
                .collect(Collectors.groupingBy(componentMetadata ->
                        componentMetadata.getNamespace() + "." + componentMetadata.getName()))
                .entrySet()
                .stream()
                .forEach(entry -> {
                    final String[] parts = entry.getKey().split("\\.");
                    if (parts.length < 2) {
                        return;
                    }
                    final List<ComponentDomain> metadataList = entry.getValue();
                    final ComponentVersionsBuilder builder =
                            ComponentVersions.builder().namespace(parts[0]).name(parts[1]);
                    metadataList.forEach(componentMetadata -> builder.version(componentMetadata.getVersion()));
                    componentVersions.add(builder.build());
                });
        return componentVersions;
    }
}
