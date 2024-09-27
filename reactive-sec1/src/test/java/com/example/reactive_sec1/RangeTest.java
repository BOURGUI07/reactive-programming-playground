package com.example.reactive_sec1;

import com.example.reactive_sec1.common.Util;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class RangeTest {
    private Flux<Integer> getItems(){
        return Flux.range(1, 50);
    }

    private Flux<Integer> getRandomItems(){
        return Flux.range(1, 50)
                .map(x-> Util.faker().random().nextInt(1,100));
    }

    @Test
    void test(){
        StepVerifier.create(getItems())
                .expectNextCount(50)
                .verifyComplete();
    }

    @Test
    void testRandom(){
        StepVerifier.create(getRandomItems())
                .expectNextMatches(x->x>0 && x<101)
                .expectNextCount(49)
                .verifyComplete();
    }

    @Test
    void testRandom1(){
        StepVerifier.create(getRandomItems())
                .thenConsumeWhile(x->x>0 && x<101)
                .verifyComplete();
    }


}
