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

package com.olacabs.fabric.jsonfilter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.olacabs.fabric.jsonfilter.impl.MatchFilter;
import org.junit.Test;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

/**
 * TODO javadoc.
 */
public class MatchFilterTest {

    private String testJson = "{ \"testMatchFilter\":{ " + "\"numberFIELD\": 24 , " + "\"booleanFIELD\": true ,"
            + " \"arrayField\":[\"a\",\"b\",\"c\"] , " + "\"stringField\": \"bbb\" , "
            + "\"objectField\": {\"abc\":\"abc\"}, " + "\"anotherNumberFIELD\": 24 , " + "\"anotherFIELD\": true , "
            + "\"anotherArrayField\":[\"a\",\"b\",\"c\"] , " + "\"anotherStringField\": \"ccc\" , "
            + "\"anotherObjectField\": {\"abc\":\"abc\"} " + "} " + "}";

    @Test
    public void testFilterWithInt() {
        Filter filter = new MatchFilter("$.testMatchFilter.numberFIELD", 24);
        assertTrue(filter.filter(testJson));
        filter = new MatchFilter("$.testMatchFilter.numberFIELD", 25);
        assertFalse(filter.filter(testJson));
    }

    @Test
    public void testFilterWithString() {
        Filter filter = new MatchFilter("$.testMatchFilter.stringField", "bbb");
        assertTrue(filter.filter(testJson));

        filter = new MatchFilter("$.testMatchFilter.stringField", "aaaaa");
        assertFalse(filter.filter(testJson));

    }

    @Test
    public void testFilterWithBoolean() {
        Filter filter = new MatchFilter("$.testMatchFilter.booleanFIELD", true);
        assertTrue(filter.filter(testJson));

        filter = new MatchFilter("$.testMatchFilter.booleanFIELD", false);
        assertFalse(filter.filter(testJson));
    }

    @Test
    public void testFilterWithArray() {
        String[] array = new String[] {"a", "b", "c"};
        Filter filter = new MatchFilter("$.testMatchFilter.arrayField", Arrays.asList(array));
        assertTrue(filter.filter(testJson));

        array = new String[] {"c", "b", "a"};
        filter = new MatchFilter("$.testMatchFilter.arrayField", Arrays.asList(array));
        assertFalse(filter.filter(testJson));

    }

    @Test
    public void testFilterWithObject() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("abc", "abc");
        Filter filter = new MatchFilter("$.testMatchFilter.objectField", map);
        assertTrue(filter.filter(testJson));

        filter = new MatchFilter("$.testMatchFilter.objectField", true);
        assertFalse(filter.filter(testJson));

    }

    @Test
    public void testFilterWithWrongField() {
        Filter filter = new MatchFilter("$.testMatchFilter.wrongField", true);
        assertFalse(filter.filter(testJson));
    }

    @Test
    public void testFilterByDsl() throws IOException, ProcessingException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("TestDsls/MatchFilterDsl.json").getFile());
        Filter filter = FilterCreator.createFilter(file);
        if (null != filter) {
            assertTrue(filter.filter(testJson));
        }
    }
}
