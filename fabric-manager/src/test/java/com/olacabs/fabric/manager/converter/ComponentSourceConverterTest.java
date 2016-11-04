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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olacabs.fabric.model.common.ComponentSource;
import com.olacabs.fabric.model.common.sources.ArtifactoryComponentSource;

/**
 * Todo .
 */
public class ComponentSourceConverterTest {

    private static ComponentSource source = ArtifactoryComponentSource.builder().artifactId("com.olacabs")
            .groupId("test").artifactoryUrl("http://test").version("0.1").build();
    private static String sourceString =
            "{\"type\":\"artifactory\",\"artifactoryUrl\":\"http://test\",\"groupId\":"
                    + "\"test\",\"artifactId\":\"com.olacabs\",\"version\":\"0.1\"}";

    @Test
    public void shouldConvertComponentSourceToDatabaseColumn() {
        final ComponentSourceConverter converter = new ComponentSourceConverter();
        final String json = converter.convertToDatabaseColumn(source);
        assertEquals(json, sourceString);
    }

    @Test
    public void shouldConvertStringToEntityAttribute() throws JsonProcessingException {
        final ComponentSourceConverter converter = new ComponentSourceConverter();
        final ComponentSource componentSource = converter.convertToEntityAttribute(sourceString);
        assertEquals(new ObjectMapper().writeValueAsString(componentSource), sourceString);
    }
}
