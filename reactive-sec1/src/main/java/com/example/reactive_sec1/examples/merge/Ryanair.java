package com.example.reactive_sec1.examples.merge;

import com.example.reactive_sec1.common.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class Ryanair {
    private static final String RYANAIR = "ryanair";

    public static Flux<Flight> ryanair() {
        return Flux.range(1, Util.faker().random().nextInt(2,22))
                .delayElements(Duration.ofMillis(Util.faker().random().nextInt(240,1100)))
                .transform(Util.fluxLogger(RYANAIR))
                .map(x-> new Flight(RYANAIR,Util.faker().random().nextInt(14,35)));
    }
}
