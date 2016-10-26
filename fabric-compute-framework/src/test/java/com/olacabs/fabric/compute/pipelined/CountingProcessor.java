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

package com.olacabs.fabric.compute.pipelined;

import com.google.common.collect.Maps;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.ScheduledProcessor;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.common.PropertyConstraint;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO doc.
 */
public class CountingProcessor extends ScheduledProcessor {
    private Map<String, Long> counts = Maps.newHashMap();

    @Override
    protected void consume(ProcessingContext context, EventSet eventSet) throws ProcessingException {
        eventSet.getEvents().forEach(event -> {
            TestEvent testEvent = (TestEvent) event;
            if (!counts.containsKey(testEvent.getMarker())) {
                counts.put(testEvent.getMarker(), 0L);
            }
            try {
                counts.put(testEvent.getMarker(), counts.get(testEvent.getMarker()) + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void initialize(String instanceId, Properties globalProperties, Properties initializationProperties,
                           ComponentMetadata componentMetadata) throws InitializationException {

    }

    @Override
    public List<Event> timeTriggerHandler(ProcessingContext context) throws ProcessingException {
        ArrayList<Event> flattenedEvent
            = counts.entrySet()
            .stream()
            .map(stringLongEntry -> Event.builder().data(stringLongEntry).build())
            .collect(Collectors.toCollection(ArrayList<Event>::new));
        counts = Maps.newHashMap();
        return flattenedEvent;
    }

    @Override
    public void destroy() {

    }

    // sample only, not used anywhere
    @Override
    public List<PropertyConstraint> getPropertyConstraints() {
        return Collections.singletonList(PropertyConstraint.builder().property("to").dependencyConstraints(Collections
                .singletonList(PropertyConstraint.DependencyConstraint.builder().property("from")
                        .operator(PropertyConstraint.Operator.GEQ).value("0").build()))
                .valueConstraint(PropertyConstraint.ValueConstraint.builder().defaultValue("100").build()).build());
    }
}
