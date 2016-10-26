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

package com.olacabs.fabric.compute.pipeline;

import com.google.common.collect.Lists;
import com.olacabs.fabric.common.util.PropertyReader;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.source.PipelineStreamSource;
import com.olacabs.fabric.model.common.ComponentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * TODO javadoc.
 */
public class ComputationPipeline {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputationPipeline.class);
    private static final long DEFAULT_WAIT_TIME_IN_SECONDS = 30;
    private final List<PipelineStreamSource> sources = Lists.newArrayList();
    private final List<PipelineStage> stages = Lists.newArrayList();

    private NotificationBus notificationBus;
    private long waitTimeInSeconds;
    private String computationName;

    public static ComputationPipeline builder() {
        return new ComputationPipeline();
    }

    public ComputationPipeline notificationBus(NotificationBus notificationBusArg) {
        this.notificationBus = notificationBusArg;
        return this;
    }

    public ComputationPipeline computationName(String computationNameArg) {
        this.computationName = computationNameArg;
        return this;
    }

    public ComputationPipeline addSource(PipelineStreamSource streamSource) {
        this.sources.add(streamSource);
        return this;
    }

    public ComputationPipeline addPipelineStage(PipelineStage pipelineStage) {
        this.stages.add(pipelineStage);
        return this;
    }

    public ComputationPipeline connect(MessageSource to, PipelineStage... pipelineStages) {
        notificationBus.connect(to, pipelineStages);
        return this;
    }

    public ComputationPipeline initialize(Properties properties) {
        properties.put("computation.name", computationName);
        waitTimeInSeconds = PropertyReader.readLong(properties, properties, "computation.shutdown.wait_time_in_seconds",
            DEFAULT_WAIT_TIME_IN_SECONDS);

        sources.forEach(streamSource -> {
            try {
                streamSource.initialize(properties);
                ComponentMetadata componentMetadata = streamSource.getSourceMetadata();
                LOGGER.info("Initialized source: {}:{}:{}->{}", componentMetadata.getNamespace(),
                        componentMetadata.getName(), componentMetadata.getVersion(), streamSource.getInstanceId());
            } catch (Exception e) {
                throw new RuntimeException(String.format("Error initializing source: %s", streamSource.getInstanceId()),
                        e);
            }
        });
        stages.forEach(stage -> {
            try {
                stage.initialize(properties);
                ComponentMetadata componentMetadata = stage.getProcessorMetadata();
                LOGGER.info("Initialized processor: {}:{}:{}->{}",
                    componentMetadata.getNamespace(),
                    componentMetadata.getName(),
                    componentMetadata.getVersion(),
                    stage.getInstanceId());
            } catch (InitializationException e) {
                throw new RuntimeException(String.format("Error initializing processor: %s", stage.getInstanceId()), e);
            }
        });
        return this;
    }

    public boolean healthcheck() {
        boolean a = true;
        try {
            for (PipelineStreamSource source : sources) {
                a = a & source.healthcheck();
                if (!a) {
                    return a;
                }
            }
            for (PipelineStage stage : stages) {
                a = a & stage.healthcheck();
                if (!a) {
                    return a;
                }
            }
        } catch (Throwable t) {
            a = false;
            LOGGER.error("Error when calling healthcheck on one of the components: ", t);
        }
        return a;
    }

    public ComputationPipeline start() {
        LOGGER.info("Starting pipeline...");
        notificationBus.start();
        stages.forEach(PipelineStage::start);
        sources.forEach(PipelineStreamSource::start);
        return this;
    }

    public void stop() {
        LOGGER.info("Stopping pipeline...");
        sources.forEach(PipelineStreamSource::stop);
        // adding sufficient sleep time for a graceful stop, allowing enough time for events in transit to be processed
        try {
            Thread.sleep(waitTimeInSeconds * 1000);
        } catch (InterruptedException iEx) {
            LOGGER.warn("Sleep was interrupted: " + iEx.getMessage());
        }
        stages.forEach(PipelineStage::stop);
        notificationBus.stop();
    }
}
