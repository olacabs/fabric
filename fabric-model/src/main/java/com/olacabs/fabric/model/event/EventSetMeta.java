package com.olacabs.fabric.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by santanu.s on 10/09/15.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventSetMeta {
    private long transactionId;
    Map<String, Object> meta;
}
