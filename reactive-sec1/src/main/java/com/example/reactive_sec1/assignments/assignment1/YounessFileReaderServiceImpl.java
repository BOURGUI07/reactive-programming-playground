package com.example.reactive_sec1.assignments.assignment1;

import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class YounessFileReaderServiceImpl implements FileReaderService {
    @Override
    public Flux<String> read(Path path) {
        return Flux.defer(() -> {
            return Flux.create(sink -> sink.onRequest(request -> {

                var counter = 0;
                Scanner scanner = null;
                try (var inputStream = Files.newInputStream(path)) {
                    scanner = new Scanner(inputStream);
                    while (scanner.hasNextLine() && counter < request && !sink.isCancelled()) {
                        sink.next(scanner.nextLine());
                        counter++;
                    }
                    sink.complete();  // Complete after reading all lines or reaching the requested amount
                } catch (IOException e) {
                    sink.error(e);  // Propagate the error downstream
                } finally {
                    if (scanner != null) scanner.close();  // Always close the scanner
                }

            }));
        });
    }
}

