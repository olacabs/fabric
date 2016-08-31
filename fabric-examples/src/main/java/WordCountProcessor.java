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

import java.util.*;

import com.google.common.base.Joiner;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.ScheduledProcessor;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import com.olacabs.fabric.model.processor.Processor;
import com.olacabs.fabric.model.processor.ProcessorType;


/**
 * A sample Processor implementation which aggregates the data
 * and generates word count.
 */
@Processor(
        namespace = "global",
        name = "word-count-processor",
        version = "0.2",
        description = "A processor that prints word frequency counts within a tumbling window",
        cpu = 0.1,
        memory = 128,
        processorType = ProcessorType.TIMER_DRIVEN,
        requiredProperties = {"triggering_frequency"},
        optionalProperties = {}
    )
public class WordCountProcessor extends ScheduledProcessor {
    private Map<String, Integer> wordCounts = new HashMap<>();

    @Override
    protected void consume(final ProcessingContext processingContext, final EventSet eventSet)
            throws ProcessingException {
        eventSet.getEvents().stream().forEach(event -> {
            String[] words = (String[]) event.getData();
            for (String word : words) {
                if (wordCounts.containsKey(word)) {
                    wordCounts.put(word, wordCounts.get(word) + 1);
                } else {
                    wordCounts.put(word, 1);
                }
            }
        });
    }

    @Override
    public void initialize(final String instanceName, final Properties global, final Properties local,
            final ComponentMetadata componentMetadata) throws InitializationException {
        // nothing to initialize here
    }

    @Override
    public List<Event> timeTriggerHandler(ProcessingContext processingContext) throws ProcessingException {
        // this method will be called after a fixed interval of time, say 5 seconds
        System.out.println(Joiner.on(",").withKeyValueSeparator("=").join(wordCounts));
        wordCounts.clear();
        // nothing to send to downstream processors
        return Collections.emptyList();

    }

    @Override
    public void destroy() {
        wordCounts.clear();
    }
}
