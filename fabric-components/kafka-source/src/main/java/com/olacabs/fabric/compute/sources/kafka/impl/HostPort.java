package com.olacabs.fabric.compute.sources.kafka.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by santanu.s on 06/10/15.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostPort {
    private String host;
    private int port;
}
