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
import com.olacabs.fabric.compute.processor.ScheduledProcessor;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by santanu.s on 10/09/15.
 */
public class SummingProcessor extends ScheduledProcessor {
    private long count = 0L;

    @Override
    protected void consume(ProcessingContext context, EventSet eventSet) throws ProcessingException {
        count += eventSet.getEvents().size();
    }

    @Override
    public void initialize(String instanceId, Properties globalProperties, Properties initializationProperties,
                           ComponentMetadata componentMetadata) throws InitializationException {

    }

    @Override
    public List<Event> timeTriggerHandler(ProcessingContext context) throws ProcessingException {
        long currentCount = count;
        count = 0L;
        return Collections.singletonList(Event.builder().data(Collections.singletonMap("count", currentCount)).build());
    }

    @Override
    public void destroy() {

    }

}
