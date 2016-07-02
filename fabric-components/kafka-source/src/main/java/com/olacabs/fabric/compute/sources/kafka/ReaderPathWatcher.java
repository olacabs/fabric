package com.olacabs.fabric.compute.sources.kafka;

import com.olacabs.fabric.compute.sources.kafka.impl.KafkaMessageReader;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by santanu.s on 12/10/15.
 */
public class ReaderPathWatcher implements PathChildrenCacheListener {
    private static final Logger logger = LoggerFactory.getLogger(ReaderPathWatcher.class);

    private final Collection<KafkaMessageReader> readers;

    public ReaderPathWatcher(Collection<KafkaMessageReader> readers) {
        this.readers = readers;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        switch (pathChildrenCacheEvent.getType()) {

            case CHILD_ADDED:
            case CHILD_UPDATED:
            case CHILD_REMOVED: {
                logger.info("Detected changes in children nodes: Will retrigger leadership");
                //readers.forEach(reader -> reader.getLeaderSelector().interruptLeadership());
                break;
            }
            case CONNECTION_SUSPENDED:
            case CONNECTION_RECONNECTED:
            case CONNECTION_LOST:
            case INITIALIZED:
                break;
        }
    }
}
