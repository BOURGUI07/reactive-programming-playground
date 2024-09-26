package com.example.reactive_sec1.examples.merge;

import com.example.reactive_sec1.common.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class Qatar {
    private static final String QATAR = "qatar";

    public static Flux<Flight> qatar() {
        return Flux.range(1, Util.faker().random().nextInt(2,17))
                .delayElements(Duration.ofMillis(Util.faker().random().nextInt(200,1000)))
                .transform(Util.fluxLogger(QATAR))
                .map(x-> new Flight(QATAR,Util.faker().random().nextInt(16,140)));
    }
}
