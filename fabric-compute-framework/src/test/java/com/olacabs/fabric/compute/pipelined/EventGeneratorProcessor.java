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

import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.StreamingProcessor;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;

import java.util.Collections;
import java.util.Properties;

public class EventGeneratorProcessor extends StreamingProcessor {
    @Override
    protected EventSet consume(ProcessingContext context, EventSet eventSet) throws ProcessingException {
        return EventSet.eventFromEventBuilder().event(Event.builder()
            .id(1L)
            .properties(Collections.singletonMap("x", "y"))
            .data(Collections.singletonMap("derived", true)).build())
            .build();
    }

    @Override
    public void initialize(String instanceId, Properties globalProperties, Properties initializationProperties, ComponentMetadata componentMetadata) throws InitializationException {

    }

    @Override
    public void destroy() {

    }
}
