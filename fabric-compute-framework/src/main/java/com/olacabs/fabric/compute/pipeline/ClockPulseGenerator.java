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

package com.olacabs.fabric.compute.pipeline;

import lombok.Builder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * TODO javadoc.
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
        clockFuture = executorService
                .scheduleAtFixedRate(() -> notificationBus.publish(PipelineMessage.timerMessageBuilder(), id),
                        notificationPeriod, notificationPeriod, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (null != clockFuture) {
            clockFuture.cancel(true);
        }
        executorService.shutdownNow();
    }
}
