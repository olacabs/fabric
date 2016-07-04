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
import com.olacabs.fabric.model.event.EventSet;

public abstract class ScheduledProcessor extends ProcessorBase {

    public ScheduledProcessor() {
        super(true);
    }

    abstract protected void consume(ProcessingContext context, EventSet eventSet) throws ProcessingException;

    @Override
    public final void process(ProcessingContext context, EventCollector eventCollector, EventSet eventSet) throws ProcessingException {
        /*if(eventSet.getType() == EventSet.Type.USER) {
            consume(context, eventSet);
            //eventCollector.publish(eventSet);
            context.acknowledge(getId(), eventSet);
        }
        else {
            EventSet generatedEventSet = timeTriggerHandler(context);
            generatedEventSet.setAggregate(true);
            eventCollector.publish(generatedEventSet);
        }*/ //PIPELINE
        consume(context, eventSet);
    }
}
