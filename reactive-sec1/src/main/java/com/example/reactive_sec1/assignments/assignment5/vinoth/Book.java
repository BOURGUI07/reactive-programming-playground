package com.example.reactive_sec1.assignments.assignment5.vinoth;

import com.example.reactive_sec1.common.Util;

public record Book(
        String genre,
        String title,
        Integer price
) {

    public static Book createBook(){
        var book = Util.faker().book();
        return new Book(book.genre(),book.title(),Util.faker().random().nextInt(10,100));
    }
}
