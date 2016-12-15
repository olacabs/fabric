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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.olacabs.fabric.jsonfilter.impl.InFilter;
import org.junit.Test;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.google.common.collect.Lists;

import net.minidev.json.JSONArray;

/**
 * TODO javadoc.
 */
public class InFilterTest {

    private String testJson = "{ \"testMatchFilter\":{ " + "\"numberFIELD\": 24 , " + "\"booleanFIELD\": true ,"
            + " \"arrayField\":[\"a\",\"b\",\"c\"] , " + "\"stringField\": \"bbb\" , "
            + "\"objectField\": {\"abc\":\"abc\"}, " + "\"anotherNumberFIELD\": 24 , " + "\"anotherFIELD\": true , "
            + "\"anotherArrayField\":[\"a\",\"b\",\"c\"] , " + "\"anotherStringField\": \"ccc\" , "
            + "\"anotherObjectField\": {\"abc\":\"abc\"} " + "} " + "}";

    private List<Object> input = Lists.newArrayList();

    @Test
    public void testFilterWithInt() {
        input.clear();
        input.add(1);
        input.add(2);
        input.add(3);
        input.add(24);
        input.add(25);
        input.add(26);
        Filter filter = new InFilter("$.testMatchFilter.numberFIELD", input);
        assertTrue(filter.filter(testJson));
        input.remove(new Integer(24));
        filter = new InFilter("$.testMatchFilter.numberFIELD", input);
        assertFalse(filter.filter(testJson));
    }

    @Test
    public void testFilterWithString() {
        input.clear();
        input.add(1);
        input.add(2);
        input.add(3);
        input.add("bbb");
        input.add(25);
        input.add(26);

        Filter filter = new InFilter("$.testMatchFilter.stringField", input);
        assertTrue(filter.filter(testJson));
        input.remove("bbb");
        filter = new InFilter("$.testMatchFilter.stringField", input);
        assertFalse(filter.filter(testJson));

    }

    @Test
    public void testFilterWithBoolean() {
        input.clear();
        input.add(1);
        input.add(2);
        input.add(true);
        input.add("bbb");
        input.add(25);
        input.add(26);

        Filter filter = new InFilter("$.testMatchFilter.booleanFIELD", input);
        assertTrue(filter.filter(testJson));
        input.remove(true);
        filter = new InFilter("$.testMatchFilter.booleanFIELD", input);
        assertFalse(filter.filter(testJson));
    }

    @Test
    public void testFilterWithArray() {
        JSONArray array = new JSONArray();
        array.add("a");
        array.add("b");
        array.add("c");
        input.clear();
        input.add(1);
        input.add(2);
        input.add(true);
        input.add("bbb");
        input.add(array);
        Filter filter = new InFilter("$.testMatchFilter.arrayField", input);
        assertTrue(filter.filter(testJson));

        array.clear();
        array.add("b");
        array.add("c");
        array.add("a");

        filter = new InFilter("$.testMatchFilter.arrayField", input);
        assertFalse(filter.filter(testJson));

    }

    @Test
    public void testFilterWithObject() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("abc", "abc");
        input.clear();
        input.add(1);
        input.add(2);
        input.add(true);
        input.add("bbb");
        input.add(map);

        Filter filter = new InFilter("$.testMatchFilter.objectField", input);
        assertTrue(filter.filter(testJson));
        map.put("1", "2");
        filter = new InFilter("$.testMatchFilter.objectField", input);
        assertFalse(filter.filter(testJson));

    }

    @Test
    public void testFilterWithWrongField() {
        Filter filter = new InFilter("$.testMatchFilter.wrongField", input);
        assertFalse(filter.filter(testJson));
    }

    @Test
    public void testFilterByDsl() throws IOException, ProcessingException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("TestDsls/InFilterDsl.json").getFile());

        Filter filter = FilterCreator.createFilter(file);
        if (null != filter) {
            assertTrue(filter.filter(testJson));
        }
    }

}
