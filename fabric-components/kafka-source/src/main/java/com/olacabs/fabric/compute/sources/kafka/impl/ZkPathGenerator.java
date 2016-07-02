package com.olacabs.fabric.compute.sources.kafka.impl;

/**
 * User: Santanu Sinha (santanu.sinha@flipkart.com)
 * Date: 23/09/13
 * Time: 9:02 PM
 */
public class ZkPathGenerator {
    private String topic;
    private String instanceId;

    public ZkPathGenerator(String topic, String instanceId) {
        this.topic = topic;
        this.instanceId = instanceId;
    }

    public String nodePath(int partition) {
        return String.format("/consumers/%s/%s/%d/nodes", topic, instanceId, partition);
    }

    public String commandPath(int partition) {
        return String.format("/consumers/%s/%s/%d/commands", topic, instanceId, partition);
    }

    public String offsetPath(int partition) {
        return String.format("/consumers/%s/%s/%d/offset", topic, instanceId, partition);
    }

    public String coordinatorPath() {
        return String.format("/consumers/%s/%s/coordinator", topic, instanceId);
    }
}
