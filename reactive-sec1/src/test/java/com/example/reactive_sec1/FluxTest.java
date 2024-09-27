package com.example.reactive_sec1;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxTest {
    private Flux<Integer> getItems(){
        return Flux.just(1, 2, 3);
    }

    @Test
    void test(){
        StepVerifier.create(getItems(),1) // Requesting ONE item
        .expectNext(1).verifyComplete();
        /*
            Won't send the complete signal, since the producer is capable
            of sending more items.
         */
    }

    @Test
    void test1(){
        StepVerifier.create(getItems(),1) // Requesting ONE item
                .expectNext(1)
                .thenCancel()// after receiving one item, we cancell
                .verify();

    }

    @Test
    void test2(){
        StepVerifier.create(getItems())
                .expectNext(1, 2, 3)//pay attention to the order, it's IMPORTANT!
                .verifyComplete();
    }




}
