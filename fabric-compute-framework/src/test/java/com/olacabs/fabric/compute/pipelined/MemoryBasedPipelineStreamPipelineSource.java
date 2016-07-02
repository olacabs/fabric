package com.olacabs.fabric.compute.pipelined;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.RawEventBundle;
import com.olacabs.fabric.compute.source.PipelineSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Created by santanu.s on 12/09/15.
 */
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryBasedPipelineStreamPipelineSource implements PipelineSource {
    private final Random random = new Random();

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

    int i = 0;
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
