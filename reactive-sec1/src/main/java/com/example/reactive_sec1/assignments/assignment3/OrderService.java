package com.example.reactive_sec1.assignments.assignment3;

import com.example.reactive_sec1.common.Util;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;

@Slf4j
public class OrderService {
    private final HashMap<String, Integer> revenueMap = new HashMap<>();
    private final HashMap<String, Integer> inventoryMap = new HashMap<>();

    private Flux<Order> emitOrders(){
        return Flux.<Order>generate(
                sink -> {
                    log.info("Emitting orders".toUpperCase());
                    var item = Util.faker().commerce().productName();
                    var category = Util.faker().commerce().department();
                    var price = Integer.parseInt(Util.faker().commerce().price());
                    var qty = Util.faker().random().nextInt(1,10);
                    sink.next(new Order(item,category,price,qty));
                }
        ).publish().refCount(2);
    }

    private void revenueService(){
        Flux.interval(Duration.ofSeconds(2))
                .flatMap(l -> emitOrders())
                .subscribe(this::consumeRevenueServiceOrders);
    }

    private void consumeRevenueServiceOrders(Order order){
        var category = order.category();
        var newRevenue = order.price()*order.quantity();
        if(revenueMap.containsKey(category)){
            revenueMap.put(category, revenueMap.get(category)+newRevenue);
        }else{
            revenueMap.put(category, newRevenue);
        }
    }

    private void inventoryService(){
        Flux.interval(Duration.ofSeconds(2))
                .flatMap(l -> emitOrders())
                .subscribe(this::consumeInventoryServiceOrder);
    }

    private void consumeInventoryServiceOrder(Order order) {
        var category = order.category();
        var quantity = order.quantity();
        var initialQty= 500;
        inventoryMap.put(category, inventoryMap.getOrDefault(category, initialQty) - quantity);
    }
}
