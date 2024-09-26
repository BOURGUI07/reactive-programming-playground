package com.example.reactive_sec1.helper;

import com.example.reactive_sec1.common.Util;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class NameGenerator1 {
    private final List redis = new ArrayList<String>();

    public Flux<String> generate() {
        return Flux.generate(sink->{
            log.info("GENERATING NAME");
            Util.sleepThread(1);
            var name = Util.faker().name().fullName();
            redis.add(name);
            sink.next(name);
                }
        )
                .startWith(redis)
                .cast(String.class);
    }
}
