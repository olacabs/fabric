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

import com.google.common.collect.ImmutableList;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.source.PipelineSource;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.RawEventBundle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Created by santanu.s on 12/09/15.
 */
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryBasedPipelineStreamPipelineSource implements PipelineSource {
    private final Random random = new Random();
    int i = 0;
    private int maxEmitCount = 10;
    private List<Event> events = ImmutableList.<Event>builder()
        .add(new TestEvent("A", 1))
        .add(new TestEvent("B", 1))
        .add(new TestEvent("C", 1))
        .add(new TestEvent("D", 1))
        .add(new TestEvent("E", 1))
        .add(new TestEvent("F", 1))
        .build();

    @Override
    public void initialize(final String instanceId, Properties globalProperties, Properties properties,
                           ProcessingContext processingContext, ComponentMetadata sourceMetadata) throws Exception {
    }

    @Override
    public RawEventBundle getNewEvents() {
        if (i++ == maxEmitCount) {
            try {
                Thread.sleep(50000);
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }

        int size = random.nextInt(5);
        size = size == 0 ? 5 : size;
        return RawEventBundle.builder()
            .events(events.subList(0, size))
            .meta(Collections.emptyMap())
            .build();
    }

}
