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

package com.olacabs.fabric.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.olacabs.fabric.model.common.sources.ArtifactoryComponentSource;
import com.olacabs.fabric.model.common.sources.JarComponentSource;
import lombok.Getter;

import javax.validation.constraints.NotNull;

/**
 * Represents a source from which the jar for a component can be downloaded.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = JarComponentSource.class, name = ComponentSourceType.JAR),
    @JsonSubTypes.Type(value = ArtifactoryComponentSource.class, name = ComponentSourceType.ARTIFACTORY),
    })

public abstract class ComponentSource {
    @JsonIgnore
    @Getter
    @NotNull
    private final ComponentSourceType type;

    protected ComponentSource(ComponentSourceType type) {
        this.type = type;
    }

    public abstract void accept(ComponentSourceVisitor visitor);
}
