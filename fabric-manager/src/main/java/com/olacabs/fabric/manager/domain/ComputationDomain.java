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
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.Setter;

/**
 * Todo .
 */
@Entity
@Table(name = "computations")
@Access(AccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputationDomain extends TenantBaseDomain {

    @NotNull
    @NotEmpty
    @Getter
    @Setter
    private String name;

    @NotNull
    @Email
    @Getter
    @Setter
    private String ownerEmail;

    @NotNull
    @Getter
    @Setter
    private String description;

    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "id")
    @Column(name = "value")
    @CollectionTable(name = "computation_attributes", joinColumns = {@JoinColumn(name = "computationId")})
    @Setter
    private Map<String, String> attributes = Maps.newHashMap();

    @Getter
    @NotNull
    @Setter
    @NotEmpty
    @JsonManagedReference
    @OrderBy(value = "id")
    @OneToMany(mappedBy = "computation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Where(clause = "type = 'SOURCE'")
    private Set<ComponentInstanceDomain> sources = Sets.newHashSet();

    @NotNull
    @NotEmpty
    @Getter
    @Setter
    @JsonManagedReference
    @OrderBy(value = "id")
    @OneToMany(mappedBy = "computation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Where(clause = "type = 'PROCESSOR'")
    private Set<ComponentInstanceDomain> processors = Sets.newHashSet();

    @NotNull
    @NotEmpty
    @Getter
    @Setter
    @JsonManagedReference
    @OneToMany(mappedBy = "computation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ConnectionDomain> connections = Sets.newHashSet();

    @Setter
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "id")
    @Column(name = "value")
    @CollectionTable(name = "computation_properties", joinColumns = {@JoinColumn(name = "computationId")})
    private Map<String, String> properties = Maps.newHashMap();

    @JsonIgnore
    @Getter
    @Setter
    private boolean deleted = false;

    @Getter
    @Setter
    private int version = 1;

    @JsonIgnore
    public ComputationDomain update(final ComputationDomain computation) {
        this.ownerEmail = computation.getOwnerEmail();
        this.description = computation.getDescription();
        if (computation.getAttributes() != null) {
            this.attributes.putAll(computation.getAttributes());
        }
        if (computation.getSources() != null) {
            this.sources.addAll(computation.getSources());
        }
        if (computation.getProcessors() != null) {
            this.processors.addAll(computation.getProcessors());
        }
        if (computation.getConnections() != null) {
            this.connections.addAll(computation.getConnections());
        }
        if (computation.getProperties() != null) {
            this.properties.putAll(computation.getProperties());
        }
        return this;
    }
}
