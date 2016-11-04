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

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Todo .
 */
public class JsonListConverterTest {

    @Test
    public void shouldConvertStringToDatabaseColumn() {
        JsonListConverter converter = new JsonListConverter();
        List<String> list = Lists.newArrayList();
        list.add("element_1");
        list.add("element_2");
        assertEquals(converter.convertToDatabaseColumn(list), "[\"element_1\",\"element_2\"]");
    }

    @Test
    public void shouldConvertStringToEntityAttribute() {
        JsonListConverter converter = new JsonListConverter();
        List list = converter.convertToEntityAttribute("[\"element_1\",\"element_2\"]");
        assertEquals(list.size(), 2);
    }

    @Test
    public void shouldConvertLongToDatabaseColumn() {
        JsonListConverter converter = new JsonListConverter();
        List<Long> list = Lists.newArrayList();
        list.add(1L);
        list.add(2L);
        assertEquals(converter.convertToDatabaseColumn(list), "[1,2]");
    }

    @Test
    public void shouldConvertLongToEntityAttribute() {
        JsonListConverter converter = new JsonListConverter();
        List list = converter.convertToEntityAttribute("[1,2]");
        assertEquals(list.size(), 2);
    }
}
