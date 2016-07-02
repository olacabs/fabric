package com.olacabs.fabric.compute.processor;

import com.olacabs.fabric.compute.EventCollector;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.model.event.EventSet;

/**
 * Created by santanu.s on 09/09/15.
 */
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
