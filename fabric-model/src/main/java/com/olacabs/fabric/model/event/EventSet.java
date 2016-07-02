package com.olacabs.fabric.model.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by santanu.s on 09/09/15.
 */
public class EventSet {
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
    private final int sourceId;

    @Getter
    private final int partitionId;

    @Getter
    private final boolean isSourceGenerated;

    @Getter
    private final List<Event> events;

    @Getter
    @Setter
    private Map<String, Object> meta;

    @Getter
    private boolean isAggregate;

    @Builder(builderMethodName = "eventFromSourceBuilder", builderClassName = "EventFromSourceBuilder")
    private EventSet(long id, int sourceId, int partitionId, @Singular List<Event> events, long transactionId, @Singular("meta") Map<String, Object> meta) {
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
