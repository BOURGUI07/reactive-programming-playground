package com.example.reactive_sec1.common;

import com.github.javafaker.Faker;
import org.reactivestreams.Subscriber;

import java.time.Duration;

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
}
