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
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * Todo .
 */
public class JsonSetConverterTest {

    @Test
    public void shouldConvertStringToDatabaseColumn() {
        JsonSetConverter converter = new JsonSetConverter();
        Set<String> set = Sets.newHashSet();
        set.add("element_1");
        set.add("element_2");
        String json = converter.convertToDatabaseColumn(set);
        boolean valid = (json.equalsIgnoreCase("[\"element_1\",\"element_2\"]")
                || json.equalsIgnoreCase("[\"element_2\",\"element_1\"]"));
        assertTrue(valid);
    }

    @Test
    public void shouldConvertStringToEntityAttribute() {
        JsonSetConverter converter = new JsonSetConverter();
        Set set = converter.convertToEntityAttribute("[\"element_1\",\"element_2\"]");
        assertEquals(set.size(), 2);
    }

    @Test
    public void shouldConvertLongToDatabaseColumn() {
        JsonSetConverter converter = new JsonSetConverter();
        Set<Long> set = Sets.newHashSet();
        set.add(1L);
        set.add(2L);
        assertEquals(converter.convertToDatabaseColumn(set), "[1,2]");
    }

    @Test
    public void shouldConvertLongToEntityAttribute() {
        JsonSetConverter converter = new JsonSetConverter();
        Set set = converter.convertToEntityAttribute("[1,2]");
        assertEquals(set.size(), 2);
    }
}
