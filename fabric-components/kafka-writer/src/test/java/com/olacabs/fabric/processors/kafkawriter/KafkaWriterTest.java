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

package com.olacabs.fabric.processors.kafkawriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.olacabs.fabric.compute.ProcessorTestBench;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import kafka.javaapi.producer.Producer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * TODO javadoc.
 */
public class KafkaWriterTest {

    private KafkaWriter processor;
    private kafka.javaapi.producer.Producer<String, String> producer;

    @Before
    public void setUp() throws InitializationException {
        processor = spy(new KafkaWriter());
        producer = mock(Producer.class);
        processor.setTopicOnJsonPath(true);
        processor.setKafkaTopic("/default");
        processor.setIngestionPoolSize(10);

        doReturn(producer).when(processor).getProducer();
        doNothing().when(producer).send(anyList());
    }

    @Test
    public void testConsume() throws Exception {
        Random rnd = new Random();
        ObjectMapper mapper = new ObjectMapper();
        List<String> lst = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            lst.add("{\"firstName\":\"" + "xyz" + rnd.nextInt(10) + "\", \"lastName\":\"Doe" + rnd.nextInt(100)
                    + "\"}");

        }

        ProcessorTestBench processorTestBench = new ProcessorTestBench(false);
        List<EventSet> events = processorTestBench.runStreamingProcessor(processor, ImmutableList
                .of(EventSet.eventFromEventBuilder().events(ImmutableList
                                .of(Event.builder().jsonNode(mapper.readTree(lst.get(0))).build(),
                                        Event.builder().jsonNode(mapper.readTree(lst.get(0))).build())).build(),
                        EventSet.eventFromEventBuilder().events(ImmutableList
                                .of(Event.builder().jsonNode(mapper.readTree(lst.get(0))).build(),
                                        Event.builder().jsonNode(mapper.readTree(lst.get(0))).build(),
                                        Event.builder().jsonNode(mapper.readTree(lst.get(0))).build())).build()));

        verify(producer, times(2)).send(anyList());


    }


}
