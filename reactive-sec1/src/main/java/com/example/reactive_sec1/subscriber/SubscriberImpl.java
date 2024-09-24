package com.example.reactive_sec1.subscriber;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Slf4j
@Getter
public class SubscriberImpl implements Subscriber<String> {
    private Subscription subscription;
    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public void onNext(String email) {
        log.info("RECEIVED EMAIL: {}" , email);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("ERROR: {}", throwable.getMessage());
    }

    @Override
    public void onComplete() {
        log.info("COMPLETED!");
    }
}
