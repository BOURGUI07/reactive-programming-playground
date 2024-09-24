package com.example.reactive_sec1.assignments.assignment1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Slf4j
public class FileReaderServiceImpl implements FileReaderService {
    @Override
    public Flux<String> read(Path path) {
        return Flux.generate(
                () -> openFile(path),
                this::readFile,
                this::closeFile

        );
    }

    private BufferedReader openFile(Path path) throws IOException {
        log.info("OPENING FILE {}", path);
        return Files.newBufferedReader(path);
    }

    private BufferedReader readFile(BufferedReader reader, SynchronousSink<String> sink){
        try{
            var line = reader.readLine();
            log.info("READING LINE {}", line);
            if(Objects.isNull(line)){
                sink.complete();
            }
            sink.next(line);
        }catch(IOException e){
            sink.error(e);
        }
        return reader;
    }

    private void closeFile(BufferedReader reader){
        try{
            log.info("CLOSING FILE {}", reader);
            reader.close();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
