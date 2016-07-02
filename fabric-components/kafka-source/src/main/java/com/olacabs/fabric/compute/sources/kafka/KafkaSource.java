package com.olacabs.fabric.compute.sources.kafka;

import com.google.common.base.Preconditions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.source.PipelineSource;
import com.olacabs.fabric.compute.sources.kafka.impl.Balancer;
import com.olacabs.fabric.compute.sources.kafka.impl.StartOffsetPickStrategy;
import com.olacabs.fabric.compute.util.ComponentPropertyReader;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.RawEventBundle;
import com.olacabs.fabric.model.source.Source;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * A balanced, partitioned source that reads off kafka.
 */
@Source(
    namespace = "global",
    name = "kafka-source",
    version = "2.1",
    description = "A partitioned kafka source",
    cpu = 2,
    memory = 1024,
    requiredProperties = {"brokers", "zookeeper", "topic-name"},
    optionalProperties = {"startOffsetPickStrategy"}
)
public class KafkaSource implements PipelineSource {

    private static final Logger logger = LoggerFactory.getLogger(KafkaSource.class);
    private static final int DEFAULT_BUFFER_SIZE = 1_048_576; //1MB

    private Balancer balancer;

    public void initialize(String instanceId, Properties globalProperties, Properties properties,
                           ProcessingContext processingContext, ComponentMetadata sourceMetadata) throws Exception {
        final CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(
            ComponentPropertyReader.readString(properties, globalProperties,
                "zookeeper", instanceId, sourceMetadata), new RetryForever(1000));
        curatorFramework.start();
        curatorFramework.blockUntilConnected();
        curatorFramework.usingNamespace("fabric");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String startOffsetPickStrategy = ComponentPropertyReader.readString(properties, globalProperties, "startOffsetPickStrategy", instanceId, sourceMetadata, StartOffsetPickStrategy.EARLIEST.toString());
        startOffsetPickStrategy = startOffsetPickStrategy.trim();
        Preconditions.checkArgument(
            StartOffsetPickStrategy.EARLIEST.toString().equalsIgnoreCase(startOffsetPickStrategy) ||
                StartOffsetPickStrategy.LATEST.toString().equalsIgnoreCase(startOffsetPickStrategy),
            String.format("startOffsetPickStrategy must be one of %s or %s", StartOffsetPickStrategy.EARLIEST.toString(),
                StartOffsetPickStrategy.LATEST.toString()));

        final int bufferSize = ComponentPropertyReader.readInteger(properties, globalProperties,
            "buffer_size", instanceId, sourceMetadata, DEFAULT_BUFFER_SIZE);
        logger.info("Buffer size is set to - {}", bufferSize);
        balancer = Balancer.builder()
            .brokers(ComponentPropertyReader.readString(
                properties, globalProperties, "brokers", instanceId, sourceMetadata))
            .curatorFramework(curatorFramework)
            .topologyName(processingContext.getTopologyName())
            .topic(ComponentPropertyReader.readString(
                properties, globalProperties, "topic-name", instanceId, sourceMetadata))
            .objectMapper(objectMapper)
            .instanceId(instanceId)
            .bufferSize(bufferSize)
            .startOffsetPickStrategy(startOffsetPickStrategy)
            .build();
        balancer.start();
    }

    public RawEventBundle getNewEvents() {
        try {
            RawEventBundle eventsBundle = balancer.getEvents().take();
            logger.info("Emitting {} events from partition {}", eventsBundle.getEvents().size(), eventsBundle.getPartitionId());
            return eventsBundle;
        } catch (InterruptedException e) {
            logger.error("Could not read event: ", e);
        }
        return null;
    }

    @Override
    public void ack(RawEventBundle rawEventBundle) {
        balancer.ack(rawEventBundle);
    }
}
