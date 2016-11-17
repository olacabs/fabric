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

package com.olacabs.fabric.manager.domain;

import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.olacabs.fabric.manager.converter.ComponentSourceConverter;
import com.olacabs.fabric.manager.converter.JsonListConverter;
import com.olacabs.fabric.model.common.ComponentSource;
import com.olacabs.fabric.model.common.ComponentType;
import com.olacabs.fabric.model.processor.ProcessorType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Todo .
 */
@Entity
@Table(name = "components")
@Access(AccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ComponentDomain extends BaseDomain {

    @NotNull
    @NotEmpty
    private String namespace;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @NotEmpty
    private String version;

    @JsonIgnore
    @Where(clause = "deleted = 0")
    private boolean deleted = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ComponentType type = ComponentType.PROCESSOR;

    private String description;

    @Enumerated(EnumType.STRING)
    private ProcessorType processorType = ProcessorType.EVENT_DRIVEN;

    @Convert(converter = JsonListConverter.class)
    private List<String> requiredProperties = Lists.newArrayList();

    @Convert(converter = JsonListConverter.class)
    private List<String> optionalProperties = Lists.newArrayList();

    private double cpu;

    private double memory;

    @NotNull
    @Convert(converter = ComponentSourceConverter.class)
    private ComponentSource source;

    @JsonIgnore
    public ComponentDomain update(final ComponentDomain component) {
        if (!Strings.isNullOrEmpty(component.getNamespace())) {
            this.namespace = component.getNamespace();
        }
        if (!Strings.isNullOrEmpty(component.getName())) {
            this.name = component.getName();
        }
        if (!Strings.isNullOrEmpty(component.getVersion())) {
            this.version = component.getVersion();
        }
        if (component.getVersion() != null) {
            this.type = component.getType();
        }
        if (!Strings.isNullOrEmpty(component.getDescription())) {
            this.description = component.getDescription();
        }
        if (component.getProcessorType() != null) {
            this.processorType = component.getProcessorType();
        }
        if (component.getRequiredProperties() != null) {
            requiredProperties.addAll(component.getRequiredProperties());
        }
        if (component.getOptionalProperties() != null) {
            optionalProperties.addAll(component.getOptionalProperties());
        }
        if (this.cpu > 0) {
            this.cpu = component.getCpu();
        }
        if (this.memory > 0) {
            this.memory = component.getMemory();
        }
        return this;
    }
}
