package com.olacabs.fabric.compute.builder.impl;

import com.olacabs.fabric.model.common.ComponentMetadata;

import java.nio.file.Path;

/**
 * Created by santanu.s on 02/10/15.
 */
public interface JarDownloader {
    Path download(final ComponentMetadata componentMetadata) throws Exception;
}
