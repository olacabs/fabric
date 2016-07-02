package com.olacabs.fabric.common.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by santanu.s on 23/09/15.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryShardInfo {
    private String environment;
}
