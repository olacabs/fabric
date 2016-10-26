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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.olacabs.fabric.compute.processor.ProcessorBase;
import com.olacabs.fabric.compute.source.PipelineSource;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.common.ComponentType;
import com.olacabs.fabric.model.processor.Processor;
import com.olacabs.fabric.model.source.Source;
import lombok.Builder;
import lombok.Data;
import org.apache.http.conn.UnsupportedSchemeException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO javadoc.
 */
public class JarScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadingLoader.class);
    private static final String PROPERTIES_FILE_NAME = "compute.properties";

    private Properties properties;

    public JarScanner() throws Exception {
        this.properties = new Properties();
        this.properties.load(JarScanner.class.getResourceAsStream("/" + PROPERTIES_FILE_NAME));
    }

    private URL[] genUrls(URL[] jarFileURLs) {
        URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL[] originalURLs = loader.getURLs();
        Set<URL> mergedJarURLs = new HashSet<URL>(originalURLs.length + jarFileURLs.length);
        mergedJarURLs.addAll(Arrays.asList(originalURLs));
        mergedJarURLs.addAll(Arrays.asList(jarFileURLs));
        return mergedJarURLs.toArray(new URL[mergedJarURLs.size()]);
    }

    private ClassLoader createClassLoader(URL[] urls) {
        URLClassLoader l = new URLClassLoader(urls);
        return l;
    }

    public List<ScanResult> loadJars(final Collection<String> urls, ClassLoader parentLoader) throws Exception {
        //URLClassLoader child = new URLClassLoader(download(urls), this.getClass().getClassLoader());
        //URLClassLoader child = new URLClassLoader(download(urls), parentLoader);
        URL[] downloadedUrls = genUrls(download(urls));
        ClassLoader child = createClassLoader(downloadedUrls);
        // Evil hack
        Thread.currentThread().setContextClassLoader(child);
        return ImmutableList.<ScanResult>builder().addAll(scanForProcessors(child, downloadedUrls))
                .addAll(scanForSources(child, downloadedUrls)).build();
    }

    URL[] download(Collection<String> urls) {

        ArrayList<URL> downloadedURLs = urls.stream().map(url -> {
            URI uri = URI.create(url);
            String downloaderImplClassName = properties.getProperty(String.format("fs.%s.impl", uri.getScheme()));
            if (null == downloaderImplClassName) {
                throw new RuntimeException(
                        new UnsupportedSchemeException(uri.getScheme() + " is not supported for downloading jars"));
            }
            try {
                Class clazz = Class.forName(downloaderImplClassName);
                if (JarDownloader.class.isAssignableFrom(clazz)) {
                    try {
                        return ((JarDownloader) clazz.newInstance()).download(url).toUri().toURL();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new RuntimeException("Unsupported implementation " + downloaderImplClassName
                            + " of " + JarDownloader.class.getSimpleName());
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        })  .collect(Collectors.toCollection(ArrayList::new));
        return downloadedURLs.toArray(new URL[downloadedURLs.size()]);
    }

    private List<ScanResult> scanForProcessors(ClassLoader classLoader, URL[] downloadedUrls) throws Exception {
        Reflections reflections = new Reflections(new ConfigurationBuilder().addClassLoader(classLoader)
                .addScanners(new SubTypesScanner(), new TypeAnnotationsScanner()).addUrls(downloadedUrls));
        Set<Class<?>> processors = Sets.intersection(reflections.getTypesAnnotatedWith(Processor.class),
                reflections.getSubTypesOf(ProcessorBase.class));

        return processors.stream().map(processor -> {
            Processor processorInfo = processor.getAnnotation(Processor.class);
            ComponentMetadata metadata =
                    ComponentMetadata.builder().type(ComponentType.PROCESSOR).namespace(processorInfo.namespace())
                            .name(processorInfo.name()).version(processorInfo.version())
                            .description(processorInfo.description()).cpu(processorInfo.cpu())
                            .memory(processorInfo.memory()).processorType(processorInfo.processorType())
                            .requiredProperties(ImmutableList.copyOf(processorInfo.requiredProperties()))
                            .optionalProperties(ImmutableList.copyOf(processorInfo.optionalProperties())).build();

            return ScanResult.builder().metadata(metadata).componentClass(processor).build();
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private List<ScanResult> scanForSources(ClassLoader classLoader, URL[] downloadedUrls) throws Exception {
        Reflections reflections = new Reflections(new ConfigurationBuilder().addClassLoader(classLoader)
                .addScanners(new SubTypesScanner(), new TypeAnnotationsScanner()).addUrls(downloadedUrls));
        Set<Class<?>> sources = Sets.intersection(reflections.getTypesAnnotatedWith(Source.class),
                reflections.getSubTypesOf(PipelineSource.class));
        return sources.stream().map(source -> {
            Source sourceInfo = source.getAnnotation(Source.class);
            ComponentMetadata metadata =
                    ComponentMetadata.builder().type(ComponentType.SOURCE).namespace(sourceInfo.namespace())
                            .name(sourceInfo.name()).version(sourceInfo.version()).description(sourceInfo.description())
                            .cpu(sourceInfo.cpu()).memory(sourceInfo.memory())
                            .requiredProperties(ImmutableList.copyOf(sourceInfo.requiredProperties()))
                            .optionalProperties(ImmutableList.copyOf(sourceInfo.optionalProperties())).build();

            return ScanResult.builder().metadata(metadata).componentClass(source).build();

        }).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Scan result class.
     */
    @Builder @Data public static class ScanResult {
        private ComponentMetadata metadata;
        private Class<?> componentClass;
    }
}
