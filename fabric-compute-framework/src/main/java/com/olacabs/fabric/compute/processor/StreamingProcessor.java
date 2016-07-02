package com.olacabs.fabric.compute.processor;

import com.olacabs.fabric.compute.EventCollector;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by santanu.s on 09/09/15.
 */
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
