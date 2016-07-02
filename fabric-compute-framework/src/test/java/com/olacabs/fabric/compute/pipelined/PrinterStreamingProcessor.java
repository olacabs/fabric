package com.olacabs.fabric.compute.pipelined;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.StreamingProcessor;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.EventSet;

import java.util.Properties;

/**
 * Created by santanu.s on 09/09/15.
 */

public class PrinterStreamingProcessor extends StreamingProcessor {
    private ObjectMapper objectMapper;

    @Override
    public void initialize(String instanceId, Properties globalProperties, Properties initializationProperties,
                           ComponentMetadata componentMetadata) throws InitializationException {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Override
    protected EventSet consume(ProcessingContext context, EventSet eventSet) throws ProcessingException {
        /*if(!eventSet.isAggregate()) {
            return  eventSet;
        }*/
        System.out.println("********");
        try {
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventSet));
            System.out.flush();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println("========");
        return eventSet;
    }

    @Override
    public void destroy() {

    }
}
