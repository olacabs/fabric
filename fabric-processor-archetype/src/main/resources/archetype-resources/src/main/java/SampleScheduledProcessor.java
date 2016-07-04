package com.olacabs.fabric;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.olacabs.fabric.common.Document;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.ScheduledProcessor;
import com.olacabs.fabric.compute.util.ComponentPropertyReader;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import com.olacabs.fabric.model.processor.Processor;
import com.olacabs.fabric.model.processor.ProcessorType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author maven archetype
 */
@EqualsAndHashCode(callSuper = false)
@Slf4j
@Data
@Processor(namespace = "global",
    name = "${artifactId}", version = "0.0.1-SNAPSHOT", description = "Skeleton Timed Processor",
    cpu = 0.1, memory = 512, processorType = ProcessorType.TIMER_DRIVEN,
    requiredProperties = {"sampleSenderName", "sampleBool", "sampleInt"},
    optionalProperties = {"sampleOptionalProp"}
)
public class SampleScheduledProcessor extends ScheduledProcessor {

    private ObjectMapper mapper;

    /* Variables to hold the properties defined in the annotation */
    private String sampleSenderName;
    private boolean sampleBool;
    private int sampleInt;
    private String sampleOptionalProp;

    private ImmutableList.Builder<Event> eventBuilder = new ImmutableList.Builder<>();

    @Override
    public void initialize(final String instanceId, final Properties globalProperties, final Properties properties,
                           final ComponentMetadata componentMetadata) throws InitializationException {

        /** Example of capturing different properties in code defined in the annotations */
        this.sampleSenderName = ComponentPropertyReader.readString(properties, globalProperties, "sampleSenderName", instanceId, componentMetadata);
        this.sampleBool = ComponentPropertyReader.readBoolean(properties, globalProperties, "sampleBool", instanceId, componentMetadata);
        this.sampleInt = ComponentPropertyReader.readInteger(properties, globalProperties, "sampleInt", instanceId, componentMetadata);

        this.sampleOptionalProp = ComponentPropertyReader.readString(properties, globalProperties, "sampleOptionalProp", instanceId, componentMetadata);

        /** You should do initialization of all the libs or clients
         this method is called once while start of a topology.
         hence all init calls must happen in this section
         **/
        this.mapper = new ObjectMapper();

    }


    @Override
    public void consume(final ProcessingContext context, final EventSet eventSet) throws ProcessingException {

        /** Below is an example to get document from event
         Put your core logic in similar fashion */
        for (final Event event : eventSet.getEvents()) {
            try {
                final Document doc = mapper.readValue((byte[]) event.getData(), Document.class);
                log.info("Document Sender - {}", doc.getMetadata().getSender());

                /**
                 Extracting all the events of a particular sender
                 */
                if (Objects.equals(doc.getMetadata().getSender(), sampleSenderName)) {
                    eventBuilder.add(event);
                }
            } catch (final IOException e) {
                log.error("Error - {}", e.getMessage(), e);
                throw new ProcessingException(e);
            }
        }
    }

    @Override
    public List<Event> timeTriggerHandler(final ProcessingContext context) throws ProcessingException {
        /**
         Time based logic here(if any), like adding an extra object node
         In below example, adding timestamp jsonNode and returning the list aggregated for the sender
         */

        final List<Event> events = eventBuilder.build();

        /** Re-initiate the eventBuilder to flush older event which is already aggregated */
        eventBuilder = new ImmutableList.Builder<>();

        events.forEach(event -> log.info("Event - {}", event.getJsonNode().asText()));

        return events;
    }

    @Override
    public void destroy() {
        /** destroy or stop your objects or clients here... */
    }
}