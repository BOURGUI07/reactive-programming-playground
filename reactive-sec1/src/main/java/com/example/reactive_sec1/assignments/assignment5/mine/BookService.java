package com.example.reactive_sec1.assignments.assignment5.mine;

import com.example.reactive_sec1.common.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookService {
    private final Map<String, Integer> map = new HashMap<>();

    private Flux<Book> getBookFlux(){
        return Flux.<Book>generate(
                synchronousSink -> {
                    var title = Util.faker().book().title();
                    var genre = Util.faker().book().genre();
                    var price = Util.faker().random().nextInt(10,100);
                    synchronousSink.next(new Book(title, genre, price));
                }
        ).filter(book-> book.genre().equalsIgnoreCase("Fantasy") || book.genre().equalsIgnoreCase("Science Fiction"))
                .doOnNext(this::saveBook)
                .take(50);
    }

    private void saveBook(Book book){
        var genre = book.genre();
        var price = book.price();
        if(map.containsKey(genre)){
            map.put(genre, map.get(genre)+price);
        }else{
            map.put(genre, price);
        }

    }

    private List<BookReport> books(){
        return map.keySet()
                .stream()
                .map(k -> new BookReport(k,map.get(k)))
                .toList();
    }

    public void bookReportStream(){
        Flux.fromIterable(books())
                .buffer(Duration.ofMillis(500))
                .subscribe(Util.subscriber());

        Util.sleepThread(60);
    }
}
