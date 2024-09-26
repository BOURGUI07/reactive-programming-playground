package com.example.reactive_sec1.common;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.UnaryOperator;

@Slf4j
public class Util {
    private static final Faker faker = Faker.instance();
    public static <T>Subscriber<T> subscriber() {
        return new DefaultSubscriber<>("");
    }

    public static <T>Subscriber<T> subscriber(String name) {
        return new DefaultSubscriber<>(name);
    }

    public static Faker faker() {
        return faker;
    }

    public static void sleepThread(int seconds) {
        try {
            Thread.sleep(Duration.ofSeconds(seconds));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleepThreadMillis(int milliseconds) {
        try {
            Thread.sleep(Duration.ofMillis(milliseconds));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T>UnaryOperator<Flux<T>> fluxLogger(String fluxName){
        return flux -> flux
                .doOnSubscribe(s-> log.info("SUBSCRIBING TO: {}", fluxName))
                .doOnCancel(() -> log.info("CANCELING SUBSCRIPTION TO: {}", fluxName))
                .doOnComplete(() -> log.info("COMPLETED : {}", fluxName));
    }
}
