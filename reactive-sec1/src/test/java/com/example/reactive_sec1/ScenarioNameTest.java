package com.example.reactive_sec1;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

public class ScenarioNameTest {
    private Flux<Integer> getItems(){
        return Flux.just(1, 2, 3);
    }

    /*
        For debugging purposes
     */
    @Test
    void test(){
        var options = StepVerifierOptions.create().scenarioName("1 to 3 range test");
        StepVerifier.create(getItems(), options)
                .expectNext(1,2)
                .as("First 2 items should be 1 and 2")
                .verifyComplete();

    }
}
