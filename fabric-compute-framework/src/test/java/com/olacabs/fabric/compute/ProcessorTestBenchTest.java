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

package com.olacabs.fabric.compute;

import com.google.common.collect.ImmutableList;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.ScheduledProcessor;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by santanu.s on 23/09/15.
 */
@Slf4j
public class ProcessorTestBenchTest {
    @Test
    public void testRunScheduledProcessor() throws Exception {
        Counter counter = new Counter();
        List<EventSet> events = new ProcessorTestBench(true).runScheduledProcessor(counter, 1000, 2, Collections.singletonList(
            EventSet.eventFromEventBuilder()
                .events(
                    ImmutableList.of(
                        Event.builder()
                            .data(Collections.singletonMap("a", 1))
                            .build()))
                .build()));
        long totalCount = events
            .stream()
            .mapToLong(eventSet -> eventSet.getEvents()
                .stream()
                .mapToLong(event -> ((Map<String, Long>) event.getData()).get("counter"))
                .sum())
            .sum();
        Assert.assertEquals(1, totalCount);
    }

    private static class Counter extends ScheduledProcessor {
        private long counter;

        @Override
        protected void consume(ProcessingContext context, EventSet eventSet) throws ProcessingException {
            counter++;
        }

        @Override
        public void initialize(String instanceId,
                               Properties globalProperties, Properties initializationProperties,
                               ComponentMetadata componentMetadata) throws InitializationException {
            counter = 0;
        }

        @Override
        public List<Event> timeTriggerHandler(ProcessingContext context) throws ProcessingException {
            long oldCounter = counter;
            counter = 0;
            return Collections.singletonList(
                Event.builder().data(Collections.singletonMap("counter", oldCounter)).build());
        }

        @Override
        public void destroy() {

        }
    }
}