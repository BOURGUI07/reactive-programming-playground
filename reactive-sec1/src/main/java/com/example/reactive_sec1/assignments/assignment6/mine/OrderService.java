package com.example.reactive_sec1.assignments.assignment6.mine;

import com.example.reactive_sec1.common.Util;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Set;

@Slf4j
public class OrderService {
    Set<String> allowedCategories = Set.of("Kids","Automotive");

    public void createOrderFlux(){
         Flux.interval(Duration.ofMillis(200))
                .map(x-> PurchaseOrder.create())
                 .filter(order -> allowedCategories.contains(order.category()))
                 .groupBy(PurchaseOrder::category)
                 .flatMap(this::processCategoryFluxes)
                 .subscribe();
        Util.sleepThread(60);
    }

    private Mono<Void> processCategoryFluxes(GroupedFlux<String, PurchaseOrder> groupedFlux){
        log.info("RECEIVED GROUPED FLUX FOR KEY: {}", groupedFlux.key());
        var groupedFluxKey = groupedFlux.key();
        var mappedFlux =  groupedFlux
                .doOnNext(item -> log.info("RECEIVED ORDER: {} FOR KEY {}",item,groupedFluxKey));

        if(groupedFluxKey.equalsIgnoreCase("Automotive")){
            return mappedFlux.map(order -> new PurchaseOrder(order.item(),order.category(),100+order.price()))
                    .doOnNext(item -> log.info("UPDATED ORDER: {} FOR KEY {}",item,groupedFluxKey))
                    .then();
        }else{
            return mappedFlux.then();
        }
    }
}
