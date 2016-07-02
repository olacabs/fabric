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

/**
 * Created by santanu.s on 10/12/15.
 */
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
