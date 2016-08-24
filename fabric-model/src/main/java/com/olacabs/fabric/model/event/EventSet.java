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

package com.olacabs.fabric.model.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TODO Add more.
 */
public final class EventSet {
    @Getter
    private final int sourceId;
    @Getter
    private final int partitionId;
    @Getter
    private final boolean isSourceGenerated;
    @Getter
    private final List<Event> events;
    @Getter
    @Setter
    private long id;
    /**
     * A gobally unique transaction id. This needs to be carried forward from parent. Will be set by system.
     * This makes sense only for transactional sources like kafka.
     */
    @Getter
    @Setter
    private long transactionId;
    @Getter
    @Setter
    private Map<String, Object> meta;

    @Getter
    private boolean isAggregate;

    @Builder(builderMethodName = "eventFromSourceBuilder", builderClassName = "EventFromSourceBuilder")
    private EventSet(long id, int sourceId, int partitionId, @Singular List<Event> events, long transactionId,
            @Singular("meta") Map<String, Object> meta) {
        this.id = id;
        this.sourceId = sourceId;
        this.partitionId = partitionId;
        this.events = events;
        this.meta = meta;
        this.isSourceGenerated = (-1 != this.sourceId);
        this.transactionId = transactionId;
    }

    @Builder(builderMethodName = "eventFromEventBuilder", builderClassName = "EventFromEventBuilder")
    private EventSet(int partitionId, @Singular List<Event> events, boolean isAggregate) {
        // Just a dummy id, not really used anywhere
        this.id = Long.MAX_VALUE;
        this.partitionId = partitionId;
        this.isAggregate = isAggregate;
        this.events = events;
        this.sourceId = -1;
        this.isSourceGenerated = false;
        this.meta = Collections.emptyMap();
    }

}
