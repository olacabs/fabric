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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.olacabs.fabric.manager.bean.ExecutorConfig;
import com.olacabs.fabric.manager.bean.RuntimeOptions;

import com.olacabs.fabric.manager.converter.ExecutorConfigConverter;
import com.olacabs.fabric.manager.converter.RuntimeOptionsConverter;
import lombok.*;

/**
 * Application domain.
 */
@Getter
@Setter
@Entity
@Table(name = "applications")
@Access(AccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDomain extends TenantBaseDomain {

    private String name;

    @OneToOne
    @JoinColumn(name = "computationId")
    private ComputationDomain computation;

    @Convert(converter = ExecutorConfigConverter.class)
    private ExecutorConfig executorConfig;

    @Convert(converter = RuntimeOptionsConverter.class)
    private RuntimeOptions runtimeOptions;

    private Boolean active = true;

    private Integer instances;

    @JsonIgnore
    public ApplicationDomain update(final ApplicationDomain application) {
        if (application.getRuntimeOptions() != null) {
            this.runtimeOptions = application.getRuntimeOptions();
        }
        if (application.getActive() != null) {
            this.active = application.getActive();
        }
        if (application.getInstances() != null) {
            this.instances = application.getInstances();
        }
        return this;
    }
}
