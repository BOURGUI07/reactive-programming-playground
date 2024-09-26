package com.example.reactive_sec1.examples.flatmap;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {
    private static final Map<Integer, List<Order>> orders = Map.of(
            1, List.of(
                    new Order(1,"product1",5),
                    new Order(1,"product2",17)
            ),
            2, List.of(
                    new Order(2,"product3",52),
                    new Order(2,"product4",7)
            ),
            3, List.of()
    );

    public static Flux<Order> getOrders(int userId) {
        return Flux.fromIterable(orders.get(userId))
                .delayElements(Duration.ofMillis(500));
    }
}
