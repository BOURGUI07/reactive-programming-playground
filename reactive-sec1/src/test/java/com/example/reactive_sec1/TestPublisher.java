package com.example.reactive_sec1;

import com.example.reactive_sec1.common.Util;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.UnaryOperator;

public class TestPublisher {
    private UnaryOperator<Flux<String>> processor(){
        return flux -> flux
                .filter(x-> x.length() > 1)
                .map(String::toUpperCase)
                .map(x-> x+":"+x.length());
    }

    @Test
    void testPublisher1(){
        var publisher = reactor.test.publisher.TestPublisher.<String>create();
        var flux = publisher.flux();
      //  publisher.next("a","b");
        //publisher.complete();
        // publisher.next() + publisher.complete() equivalent to publisher.emit()

        StepVerifier.create(flux.transform(processor()))
                .then(()->publisher.emit("hi","hello"))
                .expectNext("HI:2","HELLO:5")
                .verifyComplete();
    }

    @Test
    void testPublisherEmpty(){
        var publisher = reactor.test.publisher.TestPublisher.<String>create();
        var flux = publisher.flux();

        StepVerifier.create(flux.transform(processor()))
                .then(()->publisher.emit("a","h"))
                .verifyComplete();
    }
}
