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

package com.olacabs.fabric.compute.util;

import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.common.ComponentType;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test.
 */
public class ComponentPropertyReaderTest {

    @Test
    public void testReadString() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("source.source_1.zookeeper", "localhost");
        Properties globalProperties = new Properties();

        final String connStr = ComponentPropertyReader.readString(properties, globalProperties,
            "zookeeper", "source_1", ComponentMetadata.builder()
                .namespace("global")
                .name("source")
                .id("1234")
                .type(ComponentType.SOURCE)
                .build());
        assertEquals("localhost", connStr);
    }

    @Test
    public void testReadInteger() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("source.source_1.zookeeper", "10000");
        Properties globalProperties = new Properties();

        final int connStr = ComponentPropertyReader.readInteger(properties, globalProperties,
            "zookeeper", "source_1", ComponentMetadata.builder()
                .namespace("global")
                .name("source")
                .id("1234")
                .type(ComponentType.SOURCE)
                .build());
        assertEquals(10000, connStr);
    }

    @Test
    public void testReadIntegerFromGlobal() {
        Properties properties = new Properties();
        Properties globalProperties = new Properties();
        globalProperties.setProperty("source.source_1.brokers", "localhost:9092");
        globalProperties.setProperty("source.source_1.topic-name", "x");
        globalProperties.setProperty("processor.processor_1.queueSize", "100");
        globalProperties.setProperty("source.source_1.zookeeper", "localhost:2181");
        final int connStr = ComponentPropertyReader.readInteger(properties, globalProperties,
            "queueSize", "processor_1", ComponentMetadata.builder()
                .namespace("global")
                .name("source")
                .id("1234")
                .type(ComponentType.PROCESSOR)
                .build());
        assertEquals(100, connStr);
    }

    @Test
    public void testReadLong() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("source.source_1.zookeeper", "10000");
        Properties globalProperties = new Properties();

        final long connStr = ComponentPropertyReader.readLong(properties, globalProperties,
            "zookeeper", "source_1", ComponentMetadata.builder()
                .namespace("global")
                .name("source")
                .id("1234")
                .type(ComponentType.SOURCE)
                .build());
        assertEquals(10000L, connStr);
    }

    @Test
    public void testReadBoolean() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("source.source_1.zookeeper", "true");
        Properties globalProperties = new Properties();

        final boolean connStr = ComponentPropertyReader.readBoolean(properties, globalProperties,
            "zookeeper", "source_1", ComponentMetadata.builder()
                .namespace("global")
                .name("source")
                .id("1234")
                .type(ComponentType.SOURCE)
                .build());
        assertEquals(true, connStr);
    }
}

