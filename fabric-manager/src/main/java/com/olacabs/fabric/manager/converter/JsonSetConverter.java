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
import java.util.Collections;
import java.util.Set;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olacabs.fabric.manager.exception.FabricManagerException;

import lombok.extern.slf4j.Slf4j;

/**
 * Todo .
 */
@Slf4j
public class JsonSetConverter implements AttributeConverter<Set, String> {

    private final ObjectMapper serializer;

    public JsonSetConverter() {
        this(new ObjectMapper());
    }

    public JsonSetConverter(final ObjectMapper serializer) {
        this.serializer = serializer;
    }

    @Override
    public String convertToDatabaseColumn(final Set value) {
        try {
            return serializer.writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            log.error("Unable to parse Set - {}. Error - {}", value, e.getMessage(), e);
            throw new FabricManagerException(e);
        }
    }

    @Override
    public Set convertToEntityAttribute(final String value) {
        try {
            if (value == null) {
                return Collections.emptySet();
            }
            return serializer.readValue(value, Set.class);
        } catch (final IOException e) {
            log.error("Unable to parse value to Set - {}. Error - {}", value, e.getMessage(), e);
            throw new FabricManagerException(e);
        }
    }
}
