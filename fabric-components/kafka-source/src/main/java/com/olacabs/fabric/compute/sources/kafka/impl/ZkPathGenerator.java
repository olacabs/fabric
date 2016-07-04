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
