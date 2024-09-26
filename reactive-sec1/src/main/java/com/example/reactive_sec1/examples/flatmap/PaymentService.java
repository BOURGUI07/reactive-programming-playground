package com.example.reactive_sec1.examples.flatmap;

import com.example.reactive_sec1.common.Util;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class PaymentService {
    private final static Map<Integer,Integer> map = Map.of(
            1, Util.faker().random().nextInt(1000,10000),
            2, Util.faker().random().nextInt(1500,12000),
            3, Util.faker().random().nextInt(1000,1400)
    );

    public static Mono<Integer> getBalance(Integer userId){
        return Mono.fromSupplier(() -> map.get(userId));
    }
}
