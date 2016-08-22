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

package com.olacabs.fabric.executor;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sps.metrics.OpenTsdbReporter;
import com.github.sps.metrics.opentsdb.OpenTsdb;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.olacabs.fabric.common.util.MesosDnsResolver;
import com.olacabs.fabric.compute.builder.ComponentUrlResolver;
import com.olacabs.fabric.compute.builder.Linker;
import com.olacabs.fabric.compute.builder.impl.DownloadingLoader;
import com.olacabs.fabric.compute.pipeline.ComputationPipeline;
import com.olacabs.fabric.executor.impl.FileMetadataSource;
import com.olacabs.fabric.executor.impl.HttpMetadataSource;
import com.olacabs.fabric.model.common.ComponentSource;
import com.olacabs.fabric.model.computation.ComputationSpec;
import io.undertow.Undertow;
import io.undertow.util.Headers;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * TODO doc.
 */
public class Executor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);

    static {
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");
    }

    private final MesosDnsResolver dnsResolver;

    private final ObjectMapper objectMapper;

    private Undertow healthcheckMonitor;

    public Executor() {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);


        dnsResolver = new MesosDnsResolver();
    }

    public static void main(String[] args) throws Exception {
        try {
            new Executor().run(args);
        } catch (Throwable t) {
            LOGGER.error("Executor will exit...", t);
        }
    }

    public void startMonitor(final ComputationPipeline pipeline) {
        healthcheckMonitor = Undertow.builder().addHttpListener(8080, "0.0.0.0").setHandler(exchange -> {
            boolean result = pipeline.healthcheck();
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.setStatusCode(result ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send(result ? "alive" : "dead");
        })  .build();
        healthcheckMonitor.start();
    }

    public void stopMonitor() {
        healthcheckMonitor.stop();
    }

    public void run(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("m", "opentsdb-endpoint", true, "OpenTSDB endpoint host:port");
        options.addOption("d", "disable-metrics", true, "Disable metric completely");
        options.addOption("h", "help", false, "Print help");
        options.addOption(Option.builder("s")
            .longOpt("spec")
            .desc("Computation spec URL")
            .hasArg()
            .build());

        options.addOption(Option.builder("f")
            .longOpt("spec-file")
            .desc("Computation spec file [JSON]")
            .hasArg()
            .build());

        String opentsdbHost = null;
        int opentsdbPort = 4242;


        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = commandLineParser.parse(options, args);

        if (commandLine.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("fabric-executor", options);
            return;
        }

        if (!commandLine.hasOption("spec") && !commandLine.hasOption("spec-file")) {
            System.err.println("No computation spec is present");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("fabric-executor", options);
            return;
        }

        if (commandLine.hasOption("m")) {
            if (!commandLine.getOptionValue("m").equalsIgnoreCase("NIL")) {
                String[] tokens = commandLine.getOptionValue("m").split(":");
                if (tokens.length > 2) {
                    throw new Exception("Cannot parse opentsdb connection string");
                } else {
                    opentsdbHost = tokens[0].trim();
                    if (tokens.length == 2) {
                        opentsdbPort = Integer.valueOf(tokens[1]);
                    }
                }
                LOGGER.info("Setting opentsdb endpoint to {}:{}", opentsdbHost, opentsdbPort);
            }
        }

        if (Strings.isNullOrEmpty(opentsdbHost)) {
            LOGGER.warn("No metrics data available");
        }

        MetricRegistry registry = SharedMetricRegistries.getOrCreate("metrics-registry");
        MetadataSource metadataSource = metadataSource(commandLine);
        ComputationSpec spec = metadataSource.load(specPath(commandLine));
        DownloadingLoader loader = new DownloadingLoader();
        ImmutableSet.Builder<ComponentSource> componentSourceSetBuilder = ImmutableSet.builder();
        spec.getSources().forEach(sourceMeta -> componentSourceSetBuilder.add(sourceMeta.getMeta().getSource()));
        spec.getProcessors()
                .forEach(processorMeta -> componentSourceSetBuilder.add(processorMeta.getMeta().getSource()));
        Collection<String> resolvedUrls = ComponentUrlResolver.urls(componentSourceSetBuilder.build());
        LOGGER.info("Component Jar URLs: {}", resolvedUrls);

        loader.loadJars(resolvedUrls, Thread.currentThread().getContextClassLoader());

        Linker linker = new Linker(loader, registry);

        LOGGER.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec));
        ComputationPipeline pipeline = linker.build(spec);

        String host;
        if (null != System.getenv("HOST")) {
            host = System.getenv("HOST");
        } else {
            host = Inet4Address.getLocalHost().getHostAddress();
        }
        LOGGER.info("Setting container host to: " + host);

        boolean metricsDisabled = true;
        if (commandLine.hasOption("d")) {
            metricsDisabled = Boolean.valueOf(commandLine.getOptionValue("d"));
        }

        if (!metricsDisabled) {
            registry.register("gc", new GarbageCollectorMetricSet());
            registry.register("memory", new MemoryUsageGaugeSet());
            registry.register("threads", new ThreadStatesGaugeSet());
            registry.register("fd", new FileDescriptorRatioGauge());
            if (!Strings.isNullOrEmpty(opentsdbHost)) {
                long interval = (null != System.getenv("OPENTSDB_REPORTER_INTERVAL")) ? Long
                        .valueOf(System.getenv("OPENTSDB_REPORTER_INTERVAL")) : 60L;
                OpenTsdbReporter.forRegistry(registry)
                    .prefixedWith(spec.getName())
                    .convertRatesTo(TimeUnit.SECONDS)
                    .filter(MetricFilter.ALL)
                    .withTags(ImmutableMap.of("host", host, "topologyName", spec.getName()))
                    .build(OpenTsdb.forService(String.format("http://%s:%d", opentsdbHost, opentsdbPort)).create())
                    .start(interval, TimeUnit.SECONDS);
            } else {
                LOGGER.warn("Using console reporter");
                ConsoleReporter.forRegistry(registry)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .filter(MetricFilter.ALL)
                    .build()
                    .start(60L, TimeUnit.SECONDS);
            }
        } else {
            LOGGER.warn("Metrics disabled...");
        }

        try {
            pipeline.initialize(spec.getProperties())
                .start();
        } catch (Throwable t) {
            LOGGER.error("Couldn't start computation...", t);
            System.exit(-1);
        }

        startMonitor(pipeline);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Got shutdown signal, shutting down the computation");
            pipeline.stop();
            stopMonitor();
        }));
    }

    private MetadataSource metadataSource(CommandLine commandLine) {
        if (commandLine.hasOption("s")) {
            return new HttpMetadataSource(objectMapper, dnsResolver);
        }

        if (commandLine.hasOption("f")) {
            return new FileMetadataSource(objectMapper);
        }
        throw new IllegalArgumentException("No spec source defined. Use either -s or -f");
    }

    private String specPath(CommandLine commandLine) {
        if (commandLine.hasOption("s")) {
            return commandLine.getOptionValue("s");
        }
        if (commandLine.hasOption("f")) {
            return commandLine.getOptionValue("f");
        }
        throw new IllegalArgumentException("No spec source defined. Use either -s or -f");
    }
}
