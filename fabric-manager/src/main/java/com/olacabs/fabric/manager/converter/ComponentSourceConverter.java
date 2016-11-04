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

package com.olacabs.fabric.manager.converter;

import java.io.IOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olacabs.fabric.manager.exception.FabricManagerException;
import com.olacabs.fabric.model.common.ComponentSource;

import lombok.extern.slf4j.Slf4j;

/**
 * ComponentSourceConverter .
 * JPA converter for component source bean.
 *
 */
@Converter
@Slf4j
public class ComponentSourceConverter implements AttributeConverter<ComponentSource, String> {

    private final ObjectMapper mapper;

    public ComponentSourceConverter() {
        this(new ObjectMapper());
    }

    public ComponentSourceConverter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String convertToDatabaseColumn(final ComponentSource componentSource) {
        try {
            return mapper.writeValueAsString(componentSource);
        } catch (final JsonProcessingException e) {
            log.error("Unable to parse component source - {}", e.getMessage(), e);
            throw new FabricManagerException(e);
        }
    }

    @Override
    public ComponentSource convertToEntityAttribute(final String source) {
        try {
            return mapper.readValue(source, ComponentSource.class);
        } catch (final IOException e) {
            log.error("Unable to parse component source - {}", e.getMessage(), e);
            throw new FabricManagerException(e);
        }
    }
}
