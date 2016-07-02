package com.olacabs.fabric.compute.builder.impl;

import com.google.common.collect.Maps;
import com.olacabs.fabric.compute.builder.Loader;
import com.olacabs.fabric.compute.processor.ProcessorBase;
import com.olacabs.fabric.compute.source.PipelineSource;
import com.olacabs.fabric.model.common.ComponentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by santanu.s on 19/09/15.
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

            case PROCESSOR: {
                try {
                    ProcessorBase processorInstance = (ProcessorBase) scanResult.getComponentClass().getDeclaredConstructor().newInstance();
                    registeredProcessors.put(scanResult.getMetadata(), processorInstance);
                } catch (Exception e) {
                    throw new RuntimeException("Error creating processor: ", e);
                }
                break;
            }
            case SOURCE: {
                try {
                    PipelineSource sourceInstance = (PipelineSource) scanResult.getComponentClass().getDeclaredConstructor().newInstance();
                    registeredSources.put(scanResult.getMetadata(), sourceInstance);
                } catch (Exception e) {
                    throw new RuntimeException("Error creating processor: ", e);
                }
                break;
            }
        }
    }
}
