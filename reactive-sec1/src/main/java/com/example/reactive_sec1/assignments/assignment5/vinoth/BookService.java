package com.example.reactive_sec1.assignments.assignment5.vinoth;

import com.example.reactive_sec1.common.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BookService {
    Set<String> allowedCategories = Set.of(
            "Fantasy","Science fiction", "Suspence/Thriller"
    );
    private Flux<Book> bookFlux(){
        return Flux.interval(Duration.ofMillis(200))
                .map(i-> Book.createBook())
                .filter(book-> allowedCategories.contains(book.genre()));
    }

    private RevenueReport revenueReport(List<Book> books){
        var map = books.stream().collect(
                Collectors.groupingBy(
                        Book::genre,
                        Collectors.summingInt(Book::price)
                )
        );
        return new RevenueReport(LocalDate.now(),map);
    }

    public void generateBuffer(){
        bookFlux()
                .buffer(Duration.ofSeconds(5))
                .map(this::revenueReport)
                .subscribe(Util.subscriber());

        Util.sleepThread(60);
    }
}
