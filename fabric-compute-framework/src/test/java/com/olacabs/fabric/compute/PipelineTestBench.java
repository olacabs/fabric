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

package com.olacabs.fabric.compute;

import com.codahale.metrics.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.olacabs.fabric.compute.builder.ComponentUrlResolver;
import com.olacabs.fabric.compute.builder.Linker;
import com.olacabs.fabric.compute.builder.impl.DownloadingLoader;
import com.olacabs.fabric.compute.pipeline.ComputationPipeline;
import com.olacabs.fabric.model.common.ComponentSource;
import com.olacabs.fabric.model.computation.ComputationSpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PipelineTestBench {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final ScheduledReporter reporter;
    private final MetricRegistry metricRegistry;

    /**
     * A constructor for getting an instance of {@code PipelineTestBench}
     */
    public PipelineTestBench() {
        metricRegistry = SharedMetricRegistries.getOrCreate("metrics-registry");
        metricRegistry.timer("consume-timer");
        reporter = ConsoleReporter.forRegistry(metricRegistry)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .filter(MetricFilter.ALL)
            .build();
    }

    /**
     * A constructor for getting an instance of {@code PipelineTestBench} that writes metrics to a csv file
     *
     * @param dirPath The relative or absolute path of the directory to place csv files for each metric
     */
    public PipelineTestBench(String dirPath) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dirPath),
            "Provide a non-null and non-empty filePath");
        File dir = new File(dirPath);
        Preconditions.checkArgument(dir.exists() || dir.mkdirs(), "Provide a directory path which either exists or can be created");
        metricRegistry = SharedMetricRegistries.getOrCreate("metrics-registry");
        reporter = CsvReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .build(dir);
    }

    private void startReporter() {
        if (reporter != null) {
            reporter.start(1, TimeUnit.SECONDS);
            log.info("Metrics reporter started");
        }
    }

    private void stopReporter() {
        if (reporter != null) {
            reporter.stop();
            log.info("Metrics reporter stopped");
        }
    }

    public PipelineWrapper run(final ComputationSpec spec) throws Exception {
        DownloadingLoader loader = new DownloadingLoader();
        ImmutableSet.Builder<ComponentSource> componentSourceSetBuilder = ImmutableSet.builder();
        spec.getSources().forEach(sourceMeta -> componentSourceSetBuilder.add(sourceMeta.getMeta().getSource()));
        spec.getProcessors().forEach(processorMeta -> componentSourceSetBuilder.add(processorMeta.getMeta().getSource()));
        Collection<String> resolvedUrls = ComponentUrlResolver.urls(componentSourceSetBuilder.build());
        loader.loadJars(resolvedUrls, Thread.currentThread().getContextClassLoader());
        log.info("Component Jar URLs: {}", resolvedUrls);

        Linker linker = new Linker(loader, metricRegistry);

        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec));
        ComputationPipeline pipeline = linker.build(spec);
        startReporter();
        return new PipelineWrapper(reporter, pipeline.initialize(spec.getProperties()).start());
    }

    @AllArgsConstructor
    public static class PipelineWrapper {
        private final ScheduledReporter reporter;
        private final ComputationPipeline pipeline;

        public void stop() {
            pipeline.stop();
            reporter.stop();
        }
    }
}
