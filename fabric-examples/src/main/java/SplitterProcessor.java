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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.StreamingProcessor;
import com.olacabs.fabric.compute.util.ComponentPropertyReader;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import com.olacabs.fabric.model.processor.Processor;
import com.olacabs.fabric.model.processor.ProcessorType;

/**
 * A sample Processor implementation which
 * Gets the data (sentences) and splits based on delim.
 */
@Processor(
        namespace = "global",
        name = "splitter-processor",
        version = "0.1",
        cpu = 0.1,
        memory = 32,
        description = "A processor that splits sentences by a given delimiter",
        processorType = ProcessorType.EVENT_DRIVEN,
        requiredProperties = {},
        optionalProperties = {"delimiter"}
    )
public class SplitterProcessor extends StreamingProcessor {
    private String delimiter;

    @Override
    protected EventSet consume(final ProcessingContext processingContext, final EventSet eventSet)
            throws ProcessingException {
        List<Event> events = new ArrayList<>();
        eventSet.getEvents().stream()
                .forEach(event -> {
                    String sentence = (String) event.getData();
                    String[] words = sentence.split(delimiter);
                    events.add(Event.builder()
                            .data(words)
                            .id(Integer.MAX_VALUE)
                            .properties(Collections.emptyMap())
                            .build());
                });
        return EventSet.eventFromEventBuilder()
                .isAggregate(false)
                .partitionId(eventSet.getPartitionId())
                .events(events)
                .build();
    }

    @Override
    public void initialize(final String instanceName, final Properties global, final Properties local,
            final ComponentMetadata componentMetadata) throws InitializationException {
        delimiter =
                ComponentPropertyReader.readString(local, global, "delimiter", instanceName, componentMetadata, ",");

    }

    @Override
    public void destroy() {
        // do some cleanup if necessary
    }
}
