package com.example.reactive_sec1;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

public class TimeoutTest {
    private Flux<Integer> getItems(){
        return Flux.range(1, 5)
                .delayElements(Duration.ofMillis(200));
    }

    @Test
    void testVirtualTest1(){
        StepVerifier.create(getItems())
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify(Duration.ofMillis(1200));  // 200 millis for each item
    }
}
