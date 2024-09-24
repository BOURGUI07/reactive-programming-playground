package com.example.reactive_sec1.helper;

import com.example.reactive_sec1.common.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.function.Consumer;

public class CountryGenerator implements Consumer<FluxSink<String>> {
    private FluxSink<String> sink;
    @Override
    public void accept(FluxSink<String> stringFlux) {
        this.sink = stringFlux;
    }

    public void generateCounbtry(){
        sink.next(Util.faker().country().name());
    }
}
