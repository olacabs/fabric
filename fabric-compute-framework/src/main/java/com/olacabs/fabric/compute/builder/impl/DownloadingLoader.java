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

package com.olacabs.fabric.compute.builder.impl;

import com.google.common.collect.Maps;
import com.olacabs.fabric.compute.builder.Loader;
import com.olacabs.fabric.compute.processor.ProcessorBase;
import com.olacabs.fabric.compute.source.PipelineSource;
import com.olacabs.fabric.model.common.ComponentMetadata;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * TODO javadoc.
 */
public class DownloadingLoader implements Loader {
    private static final Comparator<ComponentMetadata> METADATA_COMPARATOR
        = (lhs, rhs) -> String.format("%s:%s:%s", lhs.getNamespace(), lhs.getName(), lhs.getVersion())
        .compareTo(String.format("%s:%s:%s", rhs.getNamespace(), rhs.getName(), rhs.getVersion()));
    private final Map<ComponentMetadata, PipelineSource> registeredSources = Maps.newTreeMap(METADATA_COMPARATOR);
    private final Map<ComponentMetadata, ProcessorBase> registeredProcessors = Maps.newTreeMap(METADATA_COMPARATOR);
    private JarScanner jarScanner;

    public DownloadingLoader() throws Exception {
        this(null);
    }

    public DownloadingLoader(final String namePrefix) throws Exception {
        HttpFileDownloader downloader = new HttpFileDownloader(namePrefix);
        this.jarScanner = new JarScanner(downloader);
    }

    @Override
    public PipelineSource loadSource(ComponentMetadata source) {
        return registeredSources.get(source);
    }

    @Override
    public ProcessorBase loadProcessor(ComponentMetadata processor) {
        return registeredProcessors.get(processor);
    }

    @Override
    public int getSourceCount() {
        return registeredSources.size();
    }

    @Override
    public int getProcessorCount() {
        return registeredProcessors.size();
    }

    public void loadJars(final Collection<String> urls, final ClassLoader classLoader) throws Exception {
        List<JarScanner.ScanResult> results = jarScanner.loadJars(urls, classLoader);
        results.forEach(this::handleScanResult);
    }

    private void handleScanResult(JarScanner.ScanResult scanResult) {
        switch (scanResult.getMetadata().getType()) {

            case PROCESSOR:
                try {
                    ProcessorBase processorInstance =
                            (ProcessorBase) scanResult.getComponentClass().getDeclaredConstructor().newInstance();
                    registeredProcessors.put(scanResult.getMetadata(), processorInstance);
                } catch (Exception e) {
                    throw new RuntimeException("Error creating processor: ", e);
                }
                break;

            case SOURCE:
                try {
                    PipelineSource sourceInstance =
                            (PipelineSource) scanResult.getComponentClass().getDeclaredConstructor().newInstance();
                    registeredSources.put(scanResult.getMetadata(), sourceInstance);
                } catch (Exception e) {
                    throw new RuntimeException("Error creating processor: ", e);
                }
                break;

            default:break;
        }
    }
}
