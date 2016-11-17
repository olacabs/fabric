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

package com.olacabs.fabric.manager.managed;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricRegistry;
import com.github.sps.metrics.OpenTsdbReporter;
import com.github.sps.metrics.opentsdb.OpenTsdb;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.olacabs.fabric.manager.config.ManagerConfig;
import com.olacabs.fabric.manager.config.OpenTsdbConfig;

import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;

/**
 * Todo .
 */
@Slf4j
public class OpenTsdbMetricReporter implements Managed {

    private final OpenTsdbConfig openTsdbConfig;
    private final MetricRegistry metricRegistry;

    private OpenTsdbReporter reporter;
    private String host;

    public OpenTsdbMetricReporter(final ManagerConfig managerConfig, final MetricRegistry metricRegistry) {
        this.openTsdbConfig = managerConfig.getOpenTsdbConfig();
        this.host = managerConfig.getServiceDiscovery().getHostname();
        if (Strings.isNullOrEmpty(host)) {
            try {
                host = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (final UnknownHostException ignore) {
                log.error("Unable to get the hostname...", ignore);
                host = "localhost";
            }
        }
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void start() throws Exception {
        if (!openTsdbConfig.isEnabled()) {
            log.info("OpenTSDB reporter is disabled...");
            return;
        }

        final ImmutableMap.Builder<String, String> tagMapBuilder = ImmutableMap.builder();
        final String appName = openTsdbConfig.getAppName();
        final String namespace = openTsdbConfig.getNamespace();
        final String module = openTsdbConfig.getPlatform();

        Preconditions.checkArgument(!Strings.isNullOrEmpty(appName), "Please provide non empty app name");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(host), "Please provide non empty host name");

        tagMapBuilder.put("host", host);
        tagMapBuilder.put("service", appName);

        if (Strings.isNullOrEmpty(namespace)) {
            tagMapBuilder.put("namespace", namespace);
        }

        if (Strings.isNullOrEmpty(module)) {
            tagMapBuilder.put("module", module);
        }

        this.reporter = OpenTsdbReporter.forRegistry(metricRegistry).prefixedWith(appName)
                .withTags(tagMapBuilder.build()).build(OpenTsdb.forService(openTsdbConfig.getOpenTsdbUrl()).create());

        this.reporter.start(openTsdbConfig.getPeriodInSeconds(), TimeUnit.SECONDS);
        log.info("OpenTSDB reporter started...");
    }

    @Override
    public void stop() throws Exception {
        if (openTsdbConfig.isEnabled()) {
            reporter.stop();
            log.info("OpenTSDB reporter stopped...");
        }
    }
}
