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
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Properties;

/**
 * Abstraction for a processor.
 */
public abstract class ProcessorBase {
    @Getter
    final boolean isScheduled;
    @Getter
    @Setter
    int id;

    protected ProcessorBase(boolean isScheduled) {
        this.isScheduled = isScheduled;
    }

    //Called once during initialization
    abstract public void initialize(String instanceId, Properties globalProperties, Properties initializationProperties,
                                    ComponentMetadata componentMetadata) throws InitializationException;

    //Called for every batch
    abstract public void process(ProcessingContext context, EventCollector eventCollector, EventSet eventSet) throws ProcessingException;

    //The following is called for same data
    abstract public List<Event> timeTriggerHandler(ProcessingContext context) throws ProcessingException;

    //Called once at the end
    abstract public void destroy();

    public boolean healthcheck() {
        return true;
    }
}
