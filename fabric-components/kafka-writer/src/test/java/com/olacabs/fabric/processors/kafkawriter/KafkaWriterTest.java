package com.olacabs.fabric.processors.kafkawriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.olacabs.fabric.compute.ProcessorTestBench;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import kafka.javaapi.producer.Producer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.*;


/**
 * Created by syed.kather on 07/12/15.
 */
public class KafkaWriterTest {

    private KafkaWriter processor;
    private kafka.javaapi.producer.Producer<String, String> producer;

    @Before
    public void setUp() throws InitializationException {
        processor = spy(new KafkaWriter());
        producer = mock(Producer.class);
        processor.setTopicOnJsonPath(true);
        processor.setKafkaTopic("/default");
        processor.setIngestionPoolSize(10);

        doReturn(producer).when(processor).getProducer();
        doNothing().when(producer).send(anyList());
    }

    @Test
    public void testConsume() throws Exception {
        Random rnd = new Random();
        ObjectMapper mapper = new ObjectMapper();
        List<String> lst = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            lst.add("{\"firstName\":\"" + "xyz" + rnd.nextInt(10) + "\", \"lastName\":\"Doe" + rnd.nextInt(100) + "\"}");

        }

        ProcessorTestBench processorTestBench = new ProcessorTestBench(false);
        List<EventSet> events = processorTestBench.runStreamingProcessor(processor, ImmutableList.of(EventSet.eventFromEventBuilder()
            .events(ImmutableList.of(Event.builder().jsonNode(mapper.readTree(lst.get(0))).build(), Event.builder().jsonNode(mapper.readTree(lst.get(0))).build()))
            .build(), EventSet.eventFromEventBuilder().events(ImmutableList
            .of(Event.builder().jsonNode(mapper.readTree(lst.get(0))).build(), Event.builder().jsonNode(mapper.readTree(lst.get(0))).build(),
                Event.builder().jsonNode(mapper.readTree(lst.get(0))).build())).build()));

        verify(producer, times(2)).send(anyList());


    }


}
