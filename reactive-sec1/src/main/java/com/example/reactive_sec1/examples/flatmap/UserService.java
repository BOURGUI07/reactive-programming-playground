package com.example.reactive_sec1.examples.flatmap;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class UserService {
    private static final Map<String,Integer> map = Map.of(
            "mike",1,
            "john",2,
            "sam",3
    );

    public static Mono<Integer> getUserId(String username) {
        return Mono.fromSupplier(() -> map.get(username));
    }

    public static Flux<User> getAllUsers(){
        return Flux.fromIterable(map.keySet())
                .map(x-> new User(map.get(x),x));
    }
}
