package com.example.reactive_sec1.assignments.assignment4;

import com.example.reactive_sec1.examples.flatmap.Order;

import java.util.List;

public record UserInformation(
        Integer userId,
        String username,
        Integer balance,
        List<Order> orders

) {
}
