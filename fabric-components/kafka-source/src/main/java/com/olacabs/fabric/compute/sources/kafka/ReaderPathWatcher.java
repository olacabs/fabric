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

package com.olacabs.fabric.compute.sources.kafka;

import com.olacabs.fabric.compute.sources.kafka.impl.KafkaMessageReader;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * TODO add doc.
 */
public class ReaderPathWatcher implements PathChildrenCacheListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderPathWatcher.class);

    private final Collection<KafkaMessageReader> readers;

    public ReaderPathWatcher(Collection<KafkaMessageReader> readers) {
        this.readers = readers;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent)
            throws Exception {
        switch (pathChildrenCacheEvent.getType()) {
            case CHILD_ADDED:
            case CHILD_UPDATED:
            case CHILD_REMOVED:
                LOGGER.info("Detected changes in children nodes: Will retrigger leadership");
                //readers.forEach(reader -> reader.getLeaderSelector().interruptLeadership());
                break;

            case CONNECTION_SUSPENDED:
            case CONNECTION_RECONNECTED:
            case CONNECTION_LOST:
            case INITIALIZED:
                break;
            default:break;
        }
    }
}
