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
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olacabs.fabric.manager.exception.FabricManagerException;

import lombok.extern.slf4j.Slf4j;

/**
 * TODO .
 */
@Converter
@Slf4j
public class JsonListConverter implements AttributeConverter<List, String> {

    private final ObjectMapper serializer;

    public JsonListConverter() {
        this(new ObjectMapper());
    }

    public JsonListConverter(final ObjectMapper serializer) {
        this.serializer = serializer;
    }

    @Override
    public String convertToDatabaseColumn(final List value) {
        try {
            return serializer.writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            log.error("Unable to parse list - {}. Error - {}", value, e.getMessage(), e);
            throw new FabricManagerException(e);
        }
    }

    @Override
    public List convertToEntityAttribute(final String value) {
        try {
            if (value == null) {
                return Collections.emptyList();
            }
            return serializer.readValue(value, List.class);
        } catch (final IOException e) {
            log.error("Unable to parse value to list - {}. Error - {}", value, e.getMessage(), e);
            throw new FabricManagerException(e);
        }
    }
}
