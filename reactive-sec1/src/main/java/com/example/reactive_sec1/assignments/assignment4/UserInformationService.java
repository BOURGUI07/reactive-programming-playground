package com.example.reactive_sec1.assignments.assignment4;

import com.example.reactive_sec1.common.Util;
import com.example.reactive_sec1.examples.flatmap.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class UserInformationService {

    private Mono<UserInformation> buildUser(User user) {
        return  Mono.zip(
                PaymentService.getBalance(user.id()),
                OrderService.getOrders(user.id()).collectList())
                .map(x-> new UserInformation(user.id(),user.username(),x.getT1(),x.getT2()));

    }

    public void buildUserInformation(){
        UserService.getAllUsers()
                .flatMap(this::buildUser)
                .subscribe(Util.subscriber());
        Util.sleepThread(5);
    }

}
