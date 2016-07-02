package com.olacabs.fabric.compute.pipeline;

import com.olacabs.fabric.model.event.EventSet;
import lombok.Builder;
import lombok.Getter;


/**
 * Created by santanu.s on 11/09/15.
 */
public class PipelineMessage {
    public enum Type {
        TIMER,
        USERSPACE
    }

    @Getter
    private final Type messageType;

    @Getter
    private final EventSet messages;

    @Getter
    private final PipelineMessage parent;

    PipelineMessage(Type messageType) {
        this.messageType = messageType;
        messages = null;
        parent = null;
    }

    public static PipelineMessage timerMessageBuilder() {
        return new PipelineMessage(Type.TIMER);
    }

    @Builder(builderMethodName = "userspaceMessageBuilder")
    public PipelineMessage(EventSet messages, PipelineMessage parent) {
        this.messageType = Type.USERSPACE;
        this.messages = messages;
        this.parent = parent;
    }

}
