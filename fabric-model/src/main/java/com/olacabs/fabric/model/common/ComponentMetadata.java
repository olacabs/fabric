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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.olacabs.fabric.model.processor.ProcessorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO add more.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentMetadata {
    @Getter
    @Setter
    @NotNull
    private String id;

    @Getter
    @Setter
    @NotNull
    private ComponentType type;

    @Getter
    @Setter
    @NotNull
    @NotEmpty
    private String namespace;

    @Getter
    @Setter
    @NotNull
    @NotEmpty
    private String name;

    @Getter
    @Setter
    @NotNull
    @NotEmpty
    private String version;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private ProcessorType processorType;

    @Getter
    @Setter
    @Deprecated
    private List<String> requiredFields = new ArrayList<>();

    @Getter
    @Setter
    private List<String> requiredProperties = new ArrayList<>();

    @Getter
    @Setter
    private List<String> optionalProperties = new ArrayList<>();

    @Getter
    @Setter
    private double cpu;

    @Getter
    @Setter
    private double memory;

    @Getter
    @Setter
    @NotNull
    private ComponentSource source;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComponentMetadata that = (ComponentMetadata) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
