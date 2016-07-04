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

import com.olacabs.fabric.model.event.EventSet;
import lombok.Builder;
import lombok.Getter;

public class PipelineMessage {
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

    @Builder(builderMethodName = "userspaceMessageBuilder")
    public PipelineMessage(EventSet messages, PipelineMessage parent) {
        this.messageType = Type.USERSPACE;
        this.messages = messages;
        this.parent = parent;
    }

    public static PipelineMessage timerMessageBuilder() {
        return new PipelineMessage(Type.TIMER);
    }

    public enum Type {
        TIMER,
        USERSPACE
    }

}
