package com.example.reactive_sec1.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Slf4j
@Getter
public class DefaultSubscriber<T> implements Subscriber<T> {
    private final String name;

    public DefaultSubscriber(String name) {
        this.name = name.isBlank()? "Anonymous":name;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T item) {
        log.info("{} RECEIVED ITEM: {}" ,name, item);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("{} CAUSED ERROR: {}",name, throwable.getMessage());
    }

    @Override
    public void onComplete() {
        log.info("COMPLETED! FOR {}",name);
    }
}
