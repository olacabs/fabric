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

import com.olacabs.fabric.jsonfilter.impl.CompareFilter;
import org.junit.Test;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

/**
 * TODO javadoc.
 */
public class CompareFilterTest {

    private String testJson = "{ \"testMatchFilter\":{ " + "\"numberFIELD\": 24 , " + "\"booleanFIELD\": true ,"
            + " \"arrayField\":[\"a\",\"b\",\"c\"] , " + "\"stringField\": \"bbb\" , "
            + "\"objectField\": {\"abc\":\"abc\"}, " + "\"anotherNumberFIELD\": 24 , " + "\"anotherFIELD\": true , "
            + "\"anotherArrayField\":[\"a\",\"b\",\"c\"] , " + "\"anotherStringField\": \"ccc\" , "
            + "\"anotherObjectField\": {\"abc\":\"abc\"} " + "} " + "}";

    @Test
    public void testFilterWithIntConstant() {
        Filter filter = new CompareFilter("$.testMatchFilter.numberFIELD", null, 24, "eq");
        assertTrue(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.numberFIELD", null, 24, "gt");
        assertFalse(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.numberFIELD", null, 24, "lt");
        assertFalse(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.numberFIELD", null, 24, "gte");
        assertTrue(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.numberFIELD", null, 24, "lte");
        assertTrue(filter.filter(testJson));

        filter = new CompareFilter("$.testMatchFilter.numberFIELD", null, 20, "eq");
        assertFalse(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.numberFIELD", null, 20, "gt");
        assertTrue(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.numberFIELD", null, 20, "lt");
        assertFalse(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.numberFIELD", null, 20, "gte");
        assertTrue(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.numberFIELD", null, 20, "lte");
        assertFalse(filter.filter(testJson));
    }

    @Test
    public void testFilterWithStringConstant() {
        Filter filter = new CompareFilter("$.testMatchFilter.stringField", null, "bbb", "eq");
        assertTrue(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.stringField", null, "bbb", "gt");
        assertFalse(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.stringField", null, "bbb", "lt");
        assertFalse(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.stringField", null, "bbb", "gte");
        assertTrue(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.stringField", null, "bbb", "lte");
        assertTrue(filter.filter(testJson));

        filter = new CompareFilter("$.testMatchFilter.stringField", null, "aaa", "eq");
        assertFalse(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.stringField", null, "aaa", "gt");
        assertTrue(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.stringField", null, "aaa", "lt");
        assertFalse(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.stringField", null, "aaa", "gte");
        assertTrue(filter.filter(testJson));
        filter = new CompareFilter("$.testMatchFilter.stringField", null, "aaa", "lte");
        assertFalse(filter.filter(testJson));
    }

    @Test
    public void testFilterWithWrongConstantType() {
        Filter filter = new CompareFilter("$.testMatchFilter.booleanFIELD", null, "aaa", "gt");
        assertFalse(filter.filter(testJson));
    }

    @Test
    public void testFilterByDsl() throws IOException, ProcessingException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("TestDsls/CompareFilterDsl.json").getFile());

        Filter filter = FilterCreator.createFilter(file);
        if (null != filter) {
            assertTrue(filter.filter(testJson));
        }
    }

}
