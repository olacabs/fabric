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

import com.olacabs.fabric.jsonfilter.impl.ExistFilter;
import org.junit.Test;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

/**
 * TODO javadoc.
 */
public class ExistFilterTest {

    private String testJson = "{ \"testMatchFilter\":{ " + "\"numberFIELD\": 24 , " + "\"booleanFIELD\": true ,"
            + " \"arrayField\":[\"a\",\"b\",\"c\"] , " + "\"stringField\": \"bbb\" , "
            + "\"objectField\": {\"abc\":\"abc\"}, " + "\"anotherNumberFIELD\": 24 , " + "\"anotherFIELD\": true , "
            + "\"anotherArrayField\":[\"a\",\"b\",\"c\"] , " + "\"anotherStringField\": \"ccc\" , "
            + "\"anotherObjectField\": {\"abc\":\"abc\"} " + "} " + "}";

    @Test
    public void testFilter() {
        Filter filter = new ExistFilter("$.testMatchFilter.objectField");
        assertTrue(filter.filter(testJson));
    }

    @Test
    public void testFilterWithWrongField() {
        Filter filter = new ExistFilter("$.testMatchFilter.wrongField");
        assertFalse(filter.filter(testJson));
    }

    @Test
    public void testFilterByDsl() throws IOException, ProcessingException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("TestDsls/ExistFilterDsl.json").getFile());
        Filter filter = FilterCreator.createFilter(file);
        if (null != filter) {
            assertTrue(filter.filter(testJson));
        }
    }

}
