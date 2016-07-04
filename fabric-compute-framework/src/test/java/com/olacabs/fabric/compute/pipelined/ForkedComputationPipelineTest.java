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

import com.olacabs.fabric.compute.builder.Linker;
import com.olacabs.fabric.compute.builder.impl.RegisteringLoader;
import com.olacabs.fabric.compute.pipeline.ComputationPipeline;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.common.ComponentType;
import com.olacabs.fabric.model.computation.ComponentInstance;
import com.olacabs.fabric.model.computation.ComputationSpec;
import com.olacabs.fabric.model.computation.Connection;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForkedComputationPipelineTest {

    @Test
    @Ignore
    public void testCheck() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("computation.shutdown.wait_time_in_seconds", "5");
        properties.put("computation.channel.channel_type", " disruptor");
        properties.put("computation.disruptor.buffer_size", "64");
        properties.put("computation.disruptor.wait_strategy", "Yield ");

        RegisteringLoader loader = RegisteringLoader.builder()
            .source("memory", new MemoryBasedPipelineStreamPipelineSource())
            .stage("generator", new EventGeneratorProcessor())
            .stage("printer", new PrinterStreamingProcessor())
            .build();

        Linker linker = new Linker(loader);

        final String sourceId = "source_1";
        final String pid1 = "generator_1";
        final String pid2 = "printer_1";

        ComputationSpec spec = ComputationSpec.builder()
            .name("test-pipeline")
            .source(
                ComponentInstance.builder()
                    .id(sourceId)
                    .meta(
                        ComponentMetadata.builder()
                            .type(ComponentType.SOURCE)
                            .id(sourceId)
                            .name("memory")
                            .build())
                    .build())
            .processor(
                ComponentInstance.builder()
                    .id(pid1)
                    .meta(
                        ComponentMetadata.builder()
                            .type(ComponentType.PROCESSOR)
                            .id(pid1)
                            .name("generator")
                            .build())
                    .build())
            .processor(
                ComponentInstance.builder()
                    .id(pid2)
                    .meta(
                        ComponentMetadata.builder()
                            .type(ComponentType.PROCESSOR)
                            .id(pid2)
                            .name("printer")
                            .build())
                    .build())
            .connection(Connection.builder().fromType(ComponentType.SOURCE).from(sourceId).to(pid1).build())
            .connection(Connection.builder().fromType(ComponentType.PROCESSOR).from(pid1).to(pid2).build())
            .properties(properties)
            .build();
        ComputationPipeline pipeline = linker.build(spec);
        pipeline.initialize(properties);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(pipeline::start);

        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(pipeline.healthcheck());
        }

        Thread.sleep(2000);

        pipeline.stop();
        executor.shutdownNow();
    }

}