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

package com.olacabs.fabric.compute.pipelined;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.StreamingProcessor;
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
