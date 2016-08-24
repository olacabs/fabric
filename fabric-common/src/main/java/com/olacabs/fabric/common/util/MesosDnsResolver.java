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

package com.olacabs.fabric.common.util;

import com.spotify.dns.DnsSrvResolvers;
import com.spotify.dns.LookupResult;
import com.spotify.dns.statistics.DnsReporter;
import com.spotify.dns.statistics.DnsTimingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 *TODO add more.
 */
@Slf4j
public class MesosDnsResolver {
    private final DnsReporter dnsReporter;

    public MesosDnsResolver() {
        this.dnsReporter = new DnsReporter() {
            @Override
            public DnsTimingContext resolveTimer() {
                return new DnsTimingContext() {
                    private final long start = System.currentTimeMillis();

                    @Override
                    public void stop() {
                        final long now = System.currentTimeMillis();
                        final long diff = now - start;
                        log.info("Request took " + diff + "ms");
                    }
                };
            }

            @Override
            public void reportEmpty() {
                log.info("Empty response from server.");
            }

            @Override
            public void reportFailure(Throwable error) {
                log.info("Error when resolving: " + error);
            }
        };
    }

    public List<LookupResult> dnsSrvLookup(String url) {
        return DnsSrvResolvers.newBuilder()
            .cachingLookups(true)
            .retainingDataOnFailures(true)
            .metered(dnsReporter)
            .dnsLookupTimeoutMillis(1000)
            .build()
            .resolve(url);
    }
}

