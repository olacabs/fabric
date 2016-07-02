package com.olacabs.fabric.executor.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olacabs.fabric.executor.MetadataSource;
import com.olacabs.fabric.model.computation.ComputationSpec;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by santanu.s on 20/11/15.
 */
public class FileMetadataSource implements MetadataSource {
    private final ObjectMapper objectMapper;

    public FileMetadataSource(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ComputationSpec load(String path) throws Exception {
        return objectMapper.readValue(Files.readAllBytes(Paths.get(path)), ComputationSpec.class);
    }
}