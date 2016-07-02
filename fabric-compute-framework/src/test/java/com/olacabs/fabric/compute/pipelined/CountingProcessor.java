package com.olacabs.fabric.compute.pipelined;

import com.google.common.collect.Maps;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.ScheduledProcessor;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by santanu.s on 10/09/15.
 */
public class CountingProcessor extends ScheduledProcessor {
    private Map<String, Long> counts = Maps.newHashMap();

    @Override
    protected void consume(ProcessingContext context, EventSet eventSet) throws ProcessingException {
        eventSet.getEvents().forEach(event -> {
            TestEvent testEvent = (TestEvent)event;
            if(!counts.containsKey(testEvent.getMarker())) {
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

}
