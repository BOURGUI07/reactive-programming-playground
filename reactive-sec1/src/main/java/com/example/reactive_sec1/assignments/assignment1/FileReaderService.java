package com.example.reactive_sec1.assignments.assignment1;

import reactor.core.publisher.Flux;

import java.nio.file.Path;

public interface FileReaderService {
    public Flux<String> read(Path path);
}
