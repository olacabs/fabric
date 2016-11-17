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

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.olacabs.fabric.model.common.ComponentType;

import lombok.Getter;
import lombok.Setter;

/**
 * Todo .
 */
@Entity
@Table(name = "connections")
@Access(AccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectionDomain extends BaseDomain {

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "computationId")
    @Getter
    @Setter
    private ComputationDomain computation;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Getter
    @Setter
    private ComponentType fromType;

    @Getter
    @Setter
    @JsonProperty("from")
    private String fromLink;

    @Getter
    @Setter
    @JsonProperty("to")
    private String toLink;
}
