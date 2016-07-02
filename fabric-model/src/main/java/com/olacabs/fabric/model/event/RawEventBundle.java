package com.olacabs.fabric.model.event;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by santanu.s on 20/09/15.
 */
@Builder
@Data
public class RawEventBundle {
    private List<Event> events;
    private int partitionId;
    private long transactionId;
    private Map<String, Object> meta = Collections.emptyMap();
}
