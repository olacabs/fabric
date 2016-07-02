package com.olacabs.fabric.common.util;

import com.spotify.dns.DnsSrvResolvers;
import com.spotify.dns.LookupResult;
import com.spotify.dns.statistics.DnsReporter;
import com.spotify.dns.statistics.DnsTimingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by santanu.s on 20/11/15.
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

