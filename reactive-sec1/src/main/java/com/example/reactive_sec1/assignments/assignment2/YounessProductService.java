package com.example.reactive_sec1.assignments.assignment2;

import com.example.reactive_sec1.common.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class YounessProductService {
    public Flux<Product> generateProducts() {
        return Flux.range(1, 4)
                .map(x -> new Product(x, Util.faker().commerce().productName()));
    }

    public Mono<String> getProductName(int id){
        return generateProducts()
                .filter(x->x.id()==id)
                .map(Product::name)
                .next()
                .timeout(Duration.ofSeconds(2),fallBack1())
                .switchIfEmpty(fallBack2());
    }

    public Mono<String> fallBack1(){
        return Mono.fromSupplier(()->Util.faker().commerce().productName());
    }

    public Mono<String> fallBack2(){
        return Mono.fromSupplier(()->Util.faker().commerce().productName());
    }
}
