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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olacabs.fabric.manager.bean.RuntimeOptions;
import com.olacabs.fabric.manager.exception.FabricManagerException;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

/**
 * Todo .
 */
@Converter
@Slf4j
public class RuntimeOptionsConverter implements AttributeConverter<RuntimeOptions, String> {

    private final ObjectMapper mapper;

    public RuntimeOptionsConverter() {
        this(new ObjectMapper());
    }

    public RuntimeOptionsConverter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String convertToDatabaseColumn(final RuntimeOptions object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (final JsonProcessingException e) {
            log.error("Unable to parse component source - {}", e.getMessage(), e);
            throw new FabricManagerException(e);
        }
    }

    @Override
    public RuntimeOptions convertToEntityAttribute(final String runtimeOptions) {
        try {
            return mapper.readValue(runtimeOptions, RuntimeOptions.class);
        } catch (final IOException e) {
            log.error("Unable to parse RuntimeOptions source - {}", e.getMessage(), e);
            throw new FabricManagerException(e);
        }
    }
}
