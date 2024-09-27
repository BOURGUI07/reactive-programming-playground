package com.example.reactive_sec1;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.util.context.Context;

public class ContextTest {
    private static Mono<String> getWelcomeMessage2() {
        return Mono.deferContextual(ctx->{
                    if(ctx.hasKey("user")) return Mono.fromSupplier(()->"Welcome: " + ctx.get("user"));
                    else return Mono.error(new RuntimeException("UNAUTHENTICATED USER"));
                }

        );

    }

    @Test
    void testValidContext() {
        var options = StepVerifierOptions.create().withInitialContext(Context.of("user","sam"));
        StepVerifier.create(getWelcomeMessage2(), options)
                .expectNext("Welcome: sam")
                .verifyComplete();
    }

    @Test
    void testEmptyContext() {
        var options = StepVerifierOptions.create().withInitialContext(Context.empty());
        StepVerifier.create(getWelcomeMessage2(), options)
                .expectErrorMessage("UNAUTHENTICATED USER")
                .verify();
    }
}
