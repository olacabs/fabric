package com.olacabs.fabric.executor;

import com.google.common.base.Strings;

/**
 * Created by guruprasad.sridharan on 14/06/16.
 */
public class MetricsPrefixGenerator {
    public static String getMetricsPrefix() {
        String application = System.getenv("application");
        String stack = System.getenv("stack");
        String version = System.getenv("version");
        return String.format("application=%s,stack=%s,version=%s", application, stack, version);
    }
}
