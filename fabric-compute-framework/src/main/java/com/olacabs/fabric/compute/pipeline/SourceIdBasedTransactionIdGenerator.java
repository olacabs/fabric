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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by santanu.s on 12/09/15.
 */
public class SourceIdBasedTransactionIdGenerator implements TransactionIdGenerator {
    private final MessageSource messageSource;
    private AtomicLong transactionId = new AtomicLong(0L);

    public SourceIdBasedTransactionIdGenerator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void seed(long seed) {
        transactionId.set(seed);
    }

    @Override
    public long transactionId() {
        return transactionId.getAndIncrement() | ((long) (messageSource.communicationId()) << ((Long.BYTES - 1) * 8));
    }
}
