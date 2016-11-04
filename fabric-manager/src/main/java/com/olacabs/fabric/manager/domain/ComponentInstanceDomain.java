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

import java.util.Map;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.olacabs.fabric.model.common.ComponentType;

import lombok.Getter;
import lombok.Setter;

/**
 * Todo .
 */
@Entity
@Table(name = "component_instances")
@Access(AccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentInstanceDomain extends BaseDomain {

    @Getter
    @Setter
    @NotNull
    @NotEmpty
    private String id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "computationId")
    @Getter
    @Setter
    private ComputationDomain computation;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Getter
    @Setter
    @JoinColumn(name = "componentId")
    @JsonProperty("meta")
    private ComponentDomain component;

    @Transient
    @NotNull
    private int componentId;

    @JsonIgnore
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private ComponentType type = ComponentType.SOURCE;

    @Getter
    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "id")
    @Column(name = "value")
    @CollectionTable(name = "component_instance_properties", joinColumns = {@JoinColumn(name = "componentInstanceId")})
    private Map<String, String> properties = Maps.newHashMap();

    @JsonIgnore
    public int getComponentId() {
        return component != null ? component.getInternalId() : componentId;
    }

    @JsonProperty
    public void setComponentId(final int componentId) {
        this.componentId = componentId;
    }
}
