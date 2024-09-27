package com.example.reactive_sec1;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

public class VirtualTimeTest {
    private Flux<Integer> getItems(){
        return Flux.range(1, 5)
                .delayElements(Duration.ofSeconds(10));
    }

    @Test
    void test(){
        StepVerifier.create(getItems())
                .expectNext(1,2,3,4,5)
                .verifyComplete();
        // Running the test gonna take 50s
    }

    @Test
    void testVirtualTest1(){
        StepVerifier.withVirtualTime(this::getItems)
                .thenAwait(Duration.ofSeconds(50))
                .expectNext(1,2,3,4,5)
                .verifyComplete();
    }

    @Test
    void testVirtualTest2(){
        StepVerifier.withVirtualTime(this::getItems)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(9))
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(1)
                .thenAwait(Duration.ofSeconds(40))
                .expectNext(2,3,4,5)
                .verifyComplete();
        /*
            Since we're explicitly saying no events would occur after 9 secs
            then we have to explicitly declare that subscription to be expected
         */
    }
}
