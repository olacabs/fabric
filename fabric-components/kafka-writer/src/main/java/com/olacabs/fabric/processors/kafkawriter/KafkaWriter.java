package com.olacabs.fabric.processors.kafkawriter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.StreamingProcessor;
import com.olacabs.fabric.compute.util.ComponentPropertyReader;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import com.olacabs.fabric.model.processor.Processor;
import com.olacabs.fabric.model.processor.ProcessorType;
import kafka.javaapi.producer.Producer;
import kafka.producer.DefaultPartitioner;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by syed.abdul.kather on 25/11/15.
 */
@Processor(
        namespace = "global",
        name = "kafka-writer",
        version = "2.0",
        description = "A processor that write data into kafka",
        cpu = 0.1,
        memory = 1,
        processorType = ProcessorType.EVENT_DRIVEN,
        requiredProperties = {
                "brokerList",
                "ingestionPoolSize",
                "kafkaKeyJsonPath"
        },
        optionalProperties = {
                "isTopicOnJsonPath",
                "topic",
                "topicJsonPath",
                "ignoreError",
                "kafkaSerializerClass",
                "ackCount"
        })
public class KafkaWriter extends StreamingProcessor {

    private static final Logger logger = LoggerFactory.getLogger(KafkaWriter.class.getSimpleName());

    private static final boolean DEFAULT_IGNORE_SERIALIZATION_ERROR = false;
    private static final boolean DEFAULT_TOPIC_ON_JSON_PATH = false;
    private static final String DEFAULT_SERIALIZER_CLASS = "kafka.serializer.StringEncoder";
    private static final String DEFAULT_KAFKA_KEY_JSON_PATH = "/metadata/partitionKey/value";
    private static final int DEFAULT_ACK_COUNT = 1;
    private static final int DEFAULT_BATCH_SIZE = 10;
    private static final String ACK_COUNT = "-1" ;

    private String kafkaKeyJsonPath;
    private boolean ignoreError;
    private ObjectMapper mapper;

    @Getter
    @Setter
    private String kafkaTopic;

    @Getter
    @Setter
    private String kafkaTopicJsonPath;

    @Getter
    @Setter
    private int ingestionPoolSize;

    @Getter
    @Setter
    private Producer<String, String> producer;

    @Getter
    @Setter
    private boolean isTopicOnJsonPath = false;

    @Override
    protected EventSet consume(ProcessingContext processingContext, EventSet eventSet) throws ProcessingException {
        final List<KeyedMessage<String, String>> messages = Lists.newArrayList();
        try {
            eventSet.getEvents().forEach(event -> {
                KeyedMessage<String, String> convertedMessage = null;
                try {
                    convertedMessage = convertEvent(event);
                } catch (ProcessingException e) {
                    logger.error("Error converting byte stream to event: ", e);
                    throw new RuntimeException(e);
                }
                if (null != convertedMessage) {
                    messages.add(convertedMessage);
                }
            });
        } catch (final Exception e) {
            logger.error("Error converting byte stream to event: ", e);
            throw new ProcessingException(e);
        }
        Lists.partition(messages, ingestionPoolSize).forEach(messageList -> getProducer().send(messageList));
        return eventSet;
    }

    /**
     * convert the event into Kafka keyed messages
     *
     * @param event to convert
     * @return KeyedMessage
     */
    protected KeyedMessage<String, String> convertEvent(Event event) throws ProcessingException {
        JsonNode eventData = event.getJsonNode();
        if (null == eventData) {
            if (event.getData() instanceof byte[]) {
                try {
                    eventData = mapper.readTree((byte[]) event.getData());
                } catch (IOException e) {
                    logger.error("Error converting byte stream to event: ", e);
                    if (!ignoreError) {
                        logger.error("Error converting byte stream to event");
                        throw new ProcessingException("Error converting byte stream to event", e);
                    }
                    return null;
                }
            } else {
                if (!ignoreError) {
                    logger.error("Error converting byte stream to event: Event is not byte stream");
                    throw new ProcessingException("Error converting byte stream to event: Event is not byte stream");
                }
                return null;
            }
        }

        final String kafkaKey = kafkaKeyJsonPath != null
                ? eventData.at(kafkaKeyJsonPath).asText().replace("\"", "")
                : eventData.at(DEFAULT_KAFKA_KEY_JSON_PATH).asText().replace("\"", "");

        final String topic = isTopicOnJsonPath()
                ? eventData.at(getKafkaTopicJsonPath()).toString().replace("\"", "")
                : getKafkaTopic().replace("\"", "");

        return new KeyedMessage<>(topic, kafkaKey, eventData.toString());
    }


    @Override
    public void initialize(String instanceId, Properties globalProperties, Properties properties, ComponentMetadata componentMetadata) throws InitializationException {

        final String kafkaBrokerList = ComponentPropertyReader
                .readString(properties, globalProperties, "brokerList", instanceId, componentMetadata);
        isTopicOnJsonPath = ComponentPropertyReader
                .readBoolean(properties, globalProperties, "isTopicOnJsonPath", instanceId, componentMetadata, DEFAULT_TOPIC_ON_JSON_PATH);

        if (!isTopicOnJsonPath) {
            kafkaTopic = ComponentPropertyReader.readString(properties, globalProperties, "topic", instanceId, componentMetadata);
            if (kafkaTopic == null) {
                logger.error("Kafka topic in properties not found");
                throw new RuntimeException("Kafka topic in properties not found");
            }
            setKafkaTopic(kafkaTopic);
        } else {
            kafkaTopicJsonPath = ComponentPropertyReader.readString(properties, globalProperties, "topicJsonPath", instanceId, componentMetadata);
            if (kafkaTopicJsonPath == null) {
                logger.error("Kafka topic json path  not found");
                throw new RuntimeException("Kafka topic json path  not found");
            }
            setKafkaTopicJsonPath(kafkaTopicJsonPath);
        }

        kafkaKeyJsonPath = ComponentPropertyReader
                .readString(properties, globalProperties, "kafkaKeyJsonPath", instanceId, componentMetadata, DEFAULT_KAFKA_KEY_JSON_PATH);
        final String kafkaSerializerClass = ComponentPropertyReader
                .readString(properties, globalProperties, "kafkaSerializerClass", instanceId, componentMetadata, DEFAULT_SERIALIZER_CLASS);
        ingestionPoolSize = ComponentPropertyReader
                .readInteger(properties, globalProperties, "ingestionPoolSize", instanceId, componentMetadata, DEFAULT_BATCH_SIZE);
        final Integer ackCount = ComponentPropertyReader
                .readInteger(properties, globalProperties, "ackCount", instanceId, componentMetadata, DEFAULT_ACK_COUNT);
        ignoreError = ComponentPropertyReader
                .readBoolean(properties, globalProperties, "ignoreError", instanceId, componentMetadata, DEFAULT_IGNORE_SERIALIZATION_ERROR);

        final Properties props = new Properties();
        props.put("metadata.broker.list", kafkaBrokerList);
        props.put("serializer.class", kafkaSerializerClass);
        props.put("partitioner.class", DefaultPartitioner.class.getName());
        props.put("request.required.acks", ACK_COUNT);
        props.put("min.isr", Integer.toString(ackCount));

        producer = new Producer<>(new ProducerConfig(props));
        mapper = new ObjectMapper();
        logger.info("Initialized kafka writer...");
    }

    @Override
    public void destroy() {
        producer.close();
        logger.info("Closed kafka writer...");
    }
}
