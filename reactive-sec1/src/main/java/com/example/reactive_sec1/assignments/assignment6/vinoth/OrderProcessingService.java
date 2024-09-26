package com.example.reactive_sec1.assignments.assignment6.vinoth;

import com.example.reactive_sec1.assignments.assignment6.mine.PurchaseOrder;
import com.example.reactive_sec1.common.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class OrderProcessingService {
    private static final Map<String,UnaryOperator<Flux<PurchaseOrder>>> PROCESSOR_MAP=
        Map.of("Kids",kidsProcessing(),
                "Automotive",automotiveProcessing());

    private static UnaryOperator<Flux<PurchaseOrder>> automotiveProcessing(){
        return flux -> flux
                .map(order -> new PurchaseOrder(order.item(),order.category(),order.price()+100));
    }

    private static UnaryOperator<Flux<PurchaseOrder>> kidsProcessing(){
        return flux -> flux
                .flatMap(o -> getKidsFreeOrder(o).flux().startWith(o));
    }

    private static Mono<PurchaseOrder> getKidsFreeOrder(PurchaseOrder order){
        return Mono.fromSupplier(() ->
                new PurchaseOrder(order.item(),order.category(),0));
    }

    private static Predicate<PurchaseOrder> canProcess(){
        return order -> PROCESSOR_MAP.containsKey(order.category());
    }

    private static UnaryOperator<Flux<PurchaseOrder>> getProcessor(String category){
        return PROCESSOR_MAP.get(category);
    }

    public void createOrderStream(){
        Flux.interval(Duration.ofMillis(200))
                .map(x-> PurchaseOrder.create())
                .filter(canProcess())
                .groupBy(PurchaseOrder::category)
                .flatMap(groupedFlux-> groupedFlux.transform(getProcessor(groupedFlux.key())))
                .subscribe(Util.subscriber());
        Util.sleepThread(60);
    }
}
