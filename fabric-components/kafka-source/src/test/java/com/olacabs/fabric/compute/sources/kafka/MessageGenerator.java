package com.olacabs.fabric.compute.sources.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;


/**
 * Created by santanu.s on 02/11/15.
 */
public class MessageGenerator {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();

        props.put("metadata.broker.list", "localhost:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", CustomPartitioner.class.getCanonicalName());
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);

        Producer<String, String> producer = new Producer<String, String>(config);
        while (true) {
            KeyedMessage<String, String> data = new KeyedMessage<String, String>("test-part-3", "xx", "{}");
            producer.send(data);
            //Thread.sleep(500);
        }

    }
}
