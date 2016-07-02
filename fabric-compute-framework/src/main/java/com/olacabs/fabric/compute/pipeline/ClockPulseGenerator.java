package com.olacabs.fabric.compute.pipeline;

import lombok.Builder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by santanu.s on 11/09/15.
 */
public class ClockPulseGenerator {
    private final int id;
    private final long notificationPeriod;
    private final NotificationBus notificationBus;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture clockFuture = null;

    @Builder
    public ClockPulseGenerator(int id, long notificationPeriod, NotificationBus notificationBus) {
        this.id = id;
        this.notificationPeriod = notificationPeriod;
        this.notificationBus = notificationBus;
    }

    public void start() {
        clockFuture = executorService.scheduleAtFixedRate(
                () -> notificationBus.publish(PipelineMessage.timerMessageBuilder(), id), notificationPeriod, notificationPeriod, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if(null != clockFuture) {
            clockFuture.cancel(true);
        }
        executorService.shutdownNow();
    }
}
