package com.olacabs.fabric.compute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by santanu.s on 08/09/15.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingContext {
    private String topologyName;
    private Map<String, Object> cache;
}
