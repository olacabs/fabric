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
    @Setter
    int id;

    @Getter
    final boolean isScheduled;

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

    public boolean healthcheck() { return true; }
}
