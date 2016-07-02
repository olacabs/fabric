package com.olacabs.fabric.executor;

import com.olacabs.fabric.model.computation.ComputationSpec;

/**
 * Created by santanu.s on 22/09/15.
 */
public interface MetadataSource {
    ComputationSpec load(final String id) throws Exception;
}
