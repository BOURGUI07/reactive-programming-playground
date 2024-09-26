package com.example.reactive_sec1.examples.merge;

import com.example.reactive_sec1.common.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class Emirates {
    private static final String EMIRATES = "emirates";

    public static Flux<Flight> emirates() {
        return Flux.range(1, Util.faker().random().nextInt(2,10))
                .delayElements(Duration.ofMillis(Util.faker().random().nextInt(200,500)))
                .transform(Util.fluxLogger(EMIRATES))
                .map(x-> new Flight(EMIRATES,Util.faker().random().nextInt(14,100)));
    }
}
