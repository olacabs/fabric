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

package com.olacabs.fabric.compute.sources.kafka;

import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.common.ComponentType;
import com.olacabs.fabric.model.event.RawEventBundle;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class KafkaSourceTest {

    @Ignore
    @Test
    public void testRun() throws Exception {
        KafkaSource kafkaSource = new KafkaSource();
        Properties properties = new Properties();
        properties.setProperty("source.test_source.brokers", "localhost:9092");
        properties.setProperty("source.test_source.topic-name", "kafka-test--3-2");
        properties.setProperty("source.test_source.zookeeper", "localhost");
        kafkaSource.initialize("test_source", properties,
            new Properties(),
            ProcessingContext.builder().topologyName("test").build(),
            ComponentMetadata.builder().id("test_source").type(ComponentType.SOURCE).build());
        AtomicLong ctr = new AtomicLong();
        while (true) {
            RawEventBundle events = kafkaSource.getNewEvents();
            //System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(events));
            System.out.println(ctr.addAndGet(events.getEvents().size()));
            kafkaSource.ack(events);
        }
    }

}
