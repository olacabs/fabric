package com.olacabs.fabric.compute.pipeline;

import com.codahale.metrics.annotation.Timed;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.WaitStrategies;
import com.olacabs.fabric.compute.EventCollector;
import com.olacabs.fabric.compute.comms.CommsMessageHandler;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.ProcessorBase;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.util.ComponentPropertyReader;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import com.olacabs.fabric.model.common.ComponentMetadata;
import io.astefanutti.metrics.aspectj.Metrics;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * A stage in the pipeline. This encapsulates a processor.
 */
@Metrics
public class PipelineStage implements CommsMessageHandler<PipelineMessage>, MessageSource {
    private static final Logger logger = LoggerFactory.getLogger(PipelineStage.class);

    @Getter
    private final String instanceId;

    @Getter
    private final Properties properties;

    private final int id = CommsIdGenerator.nextId();
    private final TransactionIdGenerator idGenerator = new SourceIdBasedTransactionIdGenerator(this);
    @Getter
    private final ComponentMetadata processorMetadata;
    private final ProcessorBase processor;
    private final NotificationBus notificationBus;
    private final ProcessingContext context;
    private final Retryer<PipelineMessage> retryer = RetryerBuilder.<PipelineMessage>newBuilder()
            .retryIfException()
            .retryIfRuntimeException()
            .withWaitStrategy(WaitStrategies.fibonacciWait(30, TimeUnit.SECONDS))
            .build();


    private ClockPulseGenerator clockPulseGenerator;

    @Builder
    public PipelineStage(
                         String instanceId,
                         Properties properties,
                         ComponentMetadata processorMetadata,
                         ProcessorBase processor,
                         NotificationBus notificationBus,
                         ProcessingContext context) {
        this.instanceId = instanceId;
        this.properties = properties;
        this.processorMetadata = processorMetadata;
        this.processor = processor;
        this.notificationBus = notificationBus;
        this.context = context;
    }

    @Override
    public int communicationId() {
        return id;
    }

    public void initialize(Properties globalProperties) throws InitializationException {
        final Long triggeringFrequency = ComponentPropertyReader.readLong(properties, globalProperties,
                                                        "triggering_frequency", instanceId, getProcessorMetadata());
        if(null != triggeringFrequency) {
            clockPulseGenerator = ClockPulseGenerator.builder()
                    .id(id)
                    .notificationBus(notificationBus)
                    .notificationPeriod(triggeringFrequency)
                    .build();
        }
        processor.initialize(instanceId, globalProperties, properties, processorMetadata);
    }

    public boolean sendsNormalMessage() {
        return !processor.isScheduled();
    }

    public void start() {
        if(null != clockPulseGenerator) {
            clockPulseGenerator.start();
        }
    }

    public void stop() {
        if(null != clockPulseGenerator) {
            clockPulseGenerator.stop();
        }
        processor.destroy();
    }

    @Override
    public String name() {
        return processorMetadata.getName();
    }

    @Override
    public void handlePipelineMessage(PipelineMessage pipelineMessage) throws Exception {
        switch (pipelineMessage.getMessageType()) {
            case TIMER: {
                //Todo::Use raw event Bundle instead
                handleTimerMessage(pipelineMessage);
                break;
            }
            case USERSPACE: {
                handleUserMessage(pipelineMessage);
                break;
            }
        }
    }

    private void handleTimerMessage(final PipelineMessage pipelineMessage) throws ProcessingException {
        try {
            retryer.call(() -> {
                try {
                    List<Event> events = processor.timeTriggerHandler(context);
                    EventSet eventSet = EventSet.eventFromEventBuilder()
                            .isAggregate(true)
                            .events(events)
                            .build();
                    eventSet.setId(idGenerator.transactionId());
                    notificationBus.publish(
                            PipelineMessage.userspaceMessageBuilder()
                                    .messages(eventSet)
                                    .build(),
                            id);
                    logger.debug("[{}][{}] Scheduled processing completed.", processorMetadata.getName(),
                            processor.getId());
                    return null;
                } catch (Throwable t) {
                    logger.error("<timeTriggerHandler()> called on [{}][{}] threw exception: ", processor.getId(), t);
                    throw t;
                }
            });
        } catch (Exception e) {
            if(e.getCause() != null) {
                logger.error("[{}][{}] error executing timeTriggerHandler()", processorMetadata.getName(),
                        processor.getId(), e.getCause());
            } else {
                logger.error("[{}][{}] error executing timeTriggerHandler()", processorMetadata.getName(),
                        processor.getId(), e);
            }
        }
    }

    @Timed(name = "${this.processorMetadata.name}")
    private void handleUserMessage(final PipelineMessage pipelineMessage) throws ProcessingException {
        PipelineMessage messageToSend = pipelineMessage;
        EventCollector eventCollector = new EventCollector();
        try {
            PipelineMessage generatedMessage = retryer.call(() -> {
                try {
                    processor.process(context, eventCollector, pipelineMessage.getMessages());
                } catch (Throwable t) {
                    logger.error("<consume()> called on [{}][{}] threw exception: ", processorMetadata.getName(),
                            processor.getId(), t);
                    throw t;
                }
                logger.debug("[{}][{}][{}] Processing completed for message.", processorMetadata.getName(),
                        processor.getId(), pipelineMessage.getMessages().getId());
                if(null != eventCollector.getEvents()) {
                    if(pipelineMessage.getMessages().getId() != eventCollector.getEvents().getId()) {
                        eventCollector.getEvents().setId(idGenerator.transactionId());
                        eventCollector.getEvents().setTransactionId(pipelineMessage.getMessages().getTransactionId());
                        return PipelineMessage.userspaceMessageBuilder()
                                .messages(eventCollector.getEvents())
                                .parent(pipelineMessage)
                                .build();
                    }
                }
                return null;
            });
            if(null != generatedMessage) {
                messageToSend = generatedMessage;
                logger.debug("[{}][{}] Setting message to newly generated message: {}",
                                processorMetadata.getName(), pipelineMessage.getMessages().getId(),
                                generatedMessage.getMessages().getId());
            }
        } catch (Exception e) {
            if(e.getCause() != null) {
                logger.error(String.format("[%s][%s][%d] error executing handleUserMessage()",
                                    processorMetadata.getName(), processor.getId(), pipelineMessage.getMessages().getId()), e.getCause());
            }
            else {
                logger.error(String.format("[%s][%s][%d] error executing handleUserMessage()",
                                    processorMetadata.getName(), processor.getId(), pipelineMessage.getMessages().getId()), e);
            }
        } finally {
            notificationBus.publish(messageToSend, id, !processor.isScheduled());
        }
    }

    public boolean healthcheck() {
        return processor.healthcheck();
    }
}
