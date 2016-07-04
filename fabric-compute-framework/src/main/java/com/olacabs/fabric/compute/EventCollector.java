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

package com.olacabs.fabric.compute;

import com.olacabs.fabric.model.event.EventSet;
import lombok.Data;

/**
 * Created by santanu.s on 08/09/15.
 */
@Data
public class EventCollector {
    private ProcessingContext processingContext;
    private EventSet events;

    public EventCollector() {

    }

    public EventCollector(ProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

    public void publish(EventSet events) {
        this.events = events;
    }

}
