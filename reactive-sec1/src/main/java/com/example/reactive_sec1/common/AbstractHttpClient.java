package com.example.reactive_sec1.common;

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.LoopResources;

public abstract class AbstractHttpClient {
    private static final String BASE_URL = "https://localhost:7070";
    protected final HttpClient httpClient;
    private final LoopResources loopResources= LoopResources.create("YOUNESS",1,true);

    public AbstractHttpClient() {
        this.httpClient = HttpClient.create().runOn(loopResources).baseUrl(BASE_URL);
    }
}
