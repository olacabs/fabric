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

package com.olacabs.fabric.compute.comms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *TODO javadoc.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommsFrameworkMessage<T> {
    private long id;
    private String source;
    private T payload;

    public static <T> void translate(CommsFrameworkMessage<T> message, long sequence,
            CommsFrameworkMessage<T> original) {
        message.setId(original.getId());
        message.setSource(original.getSource());
        message.setPayload(original.getPayload());
    }
}
