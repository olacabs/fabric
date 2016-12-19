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

package com.olacabs.fabric.jsonfilter.processors.filters;

import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.StreamingProcessor;
import com.olacabs.fabric.compute.util.ComponentPropertyReader;
import com.olacabs.fabric.jsonfilter.Filter;
import com.olacabs.fabric.jsonfilter.FilterCreator;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import com.olacabs.fabric.model.processor.Processor;
import com.olacabs.fabric.model.processor.ProcessorType;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * TODO Add Javadoc.
 */
@Processor(namespace = "global", name = "json-filter-processor", version = "1.0.0",
        description = "A processor that filters json events",
        cpu = 0.1, memory = 128, processorType = ProcessorType.EVENT_DRIVEN, requiredProperties = {"filterDsl"})
@Slf4j
@Setter
public class JsonFilterProcessor extends StreamingProcessor {

    private Filter filter;

    @Override
    public void initialize(String instanceId, Properties globalProperties,
                           Properties properties, ComponentMetadata componentMetadata)
            throws InitializationException {
        String filterDsl = ComponentPropertyReader.readString(properties,
                globalProperties, "filterDsl", instanceId, componentMetadata);

        if (null != filterDsl && StringUtils.isEmpty(filterDsl)) {
            throw new InitializationException("required Properties not found instanceId");
        }

        try {
            filter = FilterCreator.createFilter(filterDsl);

        } catch (Exception e) {
            log.error(e.getMessage()
                    + " unable to initialize processor instanceId: "
                    + instanceId);
            throw new InitializationException(e);
        }

    }

    @Override
    protected EventSet consume(ProcessingContext context, EventSet eventSet) throws ProcessingException {
        List<Event> events = eventSet
                .getEvents()
                .parallelStream()
                .filter(Objects::nonNull)
                .filter(event -> filter.filter(event.getJsonNode().toString()))
                .collect(Collectors.toList());
        return EventSet.eventFromEventBuilder().isAggregate(false)
                .partitionId(eventSet.getPartitionId()).events(events).build();
    }

    @Override
    public void destroy() {

    }
}
