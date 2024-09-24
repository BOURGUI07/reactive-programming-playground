package com.example.reactive_sec1.common;

import reactor.core.publisher.Mono;

public class ConcreteHttpClient extends AbstractHttpClient {
    public Mono<String> getProductName(int id) {
        return this.httpClient
                .get()
                .uri("/demo0/product/" + id)
                .responseContent()
                .asString()
                .next(); //convert flux to mono
    }
}
