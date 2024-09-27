package com.example.reactive_sec1;

import com.example.reactive_sec1.common.Util;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Objects;

public class AssertNextTest {
    record Book(int id, String title, String author) {}
    private Flux<Book> getBooks(){
        var book = Util.faker().book();
        return Flux.range(1,3)
                .map(i-> new Book(i,book.title(),book.author()));
    }

    @Test
    void assertNextTest(){
        StepVerifier.create(getBooks())
                .assertNext((Book book) -> Assertions.assertEquals(1,book.id))
                .thenConsumeWhile(book -> Objects.nonNull(book.author()) && Objects.nonNull(book.title()))
                .verifyComplete();

    }

    @Test
    void collectAllAndTest(){
        StepVerifier.create(getBooks().collectList())
                .assertNext(list -> Assertions.assertEquals(3,list.size()))
                .verifyComplete();

    }

    @Test
    void collectAllAndTest1(){
        StepVerifier.create(getBooks().collectList())
                .assertNext(list -> Assertions.assertTrue(list.stream().allMatch(book -> Objects.nonNull(book.author()) && Objects.nonNull(book.title()))))
                .verifyComplete();

    }
}
