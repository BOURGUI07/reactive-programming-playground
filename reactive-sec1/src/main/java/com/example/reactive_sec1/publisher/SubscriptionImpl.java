package com.example.reactive_sec1.publisher;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Slf4j
public class SubscriptionImpl implements Subscription {
    private final Subscriber<? super String> subscriber;
    private boolean cancelled;
    private final Faker faker;
    private final static int MAX_ITEMS = 10;
    private int count;

    public SubscriptionImpl(Subscriber<? super String> subscriber) {
        this.subscriber = subscriber;
        this.faker = Faker.instance();
    }

    @Override
    public void request(long l) {
        if(cancelled) return;
        log.info("SUBSCRIBER REQUESTED {} ITEMS", l);
        if(l>MAX_ITEMS){
            subscriber.onError(new IllegalArgumentException("Maximum items exceeded".toUpperCase()));
            cancelled = true;
            return;
        }
        for(int i=0;i<l && count<MAX_ITEMS;i++) {
            count++;
            subscriber.onNext(faker.internet().emailAddress());
        }
        if(count==MAX_ITEMS) {
            log.info("NO MORE DATA TO PRODUCE, MAX REQUEST NUMBER IS REACHED");
            subscriber.onComplete();
            this.cancelled = true;
        }
    }

    @Override
    public void cancel() {
        log.info("Subscription cancelled".toUpperCase());
        cancelled = true;
    }
}
