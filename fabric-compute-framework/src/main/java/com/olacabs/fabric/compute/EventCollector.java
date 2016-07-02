package com.olacabs.fabric.compute;

import com.olacabs.fabric.model.event.EventSet;
import lombok.Data;

/**
 * Created by santanu.s on 08/09/15.
 */
@Data
public class EventCollector {
    private ProcessingContext processingContext;
    private EventSet events;

    public EventCollector() {

    }

    public EventCollector(ProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

    public void publish(EventSet events) {
        this.events = events;
    }

}
