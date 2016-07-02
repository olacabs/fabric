package com.olacabs.fabric.compute.sources.kafka;

import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.common.ComponentType;
import com.olacabs.fabric.model.event.RawEventBundle;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by santanu.s on 03/11/15.
 */
public class KafkaSourceTest {

    @Ignore
    @Test
    public void testRun() throws Exception {
        KafkaSource kafkaSource = new KafkaSource();
        Properties properties = new Properties();
        properties.setProperty("source.test_source.brokers", "localhost:9092");
        properties.setProperty("source.test_source.topic-name", "kafka-test--3-2");
        properties.setProperty("source.test_source.zookeeper", "localhost");
        kafkaSource.initialize("test_source", properties,
                new Properties(),
                ProcessingContext.builder().topologyName("test").build(),
                ComponentMetadata.builder().id("test_source").type(ComponentType.SOURCE).build());
        AtomicLong ctr = new AtomicLong();
        while(true) {
            RawEventBundle events = kafkaSource.getNewEvents();
            //System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(events));
            System.out.println(ctr.addAndGet(events.getEvents().size()));
            kafkaSource.ack(events);
        }
    }

}