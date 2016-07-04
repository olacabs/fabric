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

package com.olacabs.fabric.compute.processor;

import com.olacabs.fabric.compute.EventCollector;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public abstract class StreamingProcessor extends ProcessorBase {
    private static final Logger logger = LoggerFactory.getLogger(StreamingProcessor.class);

    public StreamingProcessor() {
        super(false);
    }

    abstract protected EventSet consume(ProcessingContext context, EventSet eventSet) throws ProcessingException;

    @Override
    public void process(ProcessingContext context, EventCollector eventCollector, EventSet eventSet) throws ProcessingException {
        eventCollector.publish(consume(context, eventSet));
        //context.acknowledge(getId(), eventSet);
    }

    @Override
    public final List<Event> timeTriggerHandler(ProcessingContext context) {
        logger.warn("timeTriggerHandler() called on StreamingProcessor: " + getId());
        return Collections.emptyList();
    }
}
