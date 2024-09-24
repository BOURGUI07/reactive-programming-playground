package com.example.reactive_sec1.helper;

import com.example.reactive_sec1.common.Util;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class NameGenerator {

    public static String generateName() {
        Util.sleepThread(1);
        return Util.faker().name().firstName();
    }

    public static List<String> nameList(int count) {
        return IntStream.range(1,count).mapToObj(x->generateName()).toList();
    }

    public static Flux<String> nameFlux(int count) {
        return Flux.range(1,count).map(x->generateName());
    }
}
