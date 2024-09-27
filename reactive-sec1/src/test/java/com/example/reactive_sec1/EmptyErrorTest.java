package com.example.reactive_sec1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class EmptyErrorTest {
    private Mono<String> getUsername(int userId){
        return switch(userId){
            case 2 -> Mono.empty();
            case 1 -> Mono.just("sam");
            default -> Mono.error(new RuntimeException("oops"));
        };
    }

    @Test
    void testValidInput(){
        StepVerifier.create(getUsername(1))
                .expectNext("sam")
                .expectComplete()
                .verify();
    }

    @Test
    void testOutputLength(){
        StepVerifier.create(getUsername(1))
                .consumeNextWith(value-> {
                    Assertions.assertEquals("sam", value);
                    Assertions.assertEquals(3, value.length());
                })
                .expectComplete()
                .verify();
    }

    @Test
    void testEmpty(){
        StepVerifier.create(getUsername(2))
                .expectComplete()
                .verify();
    }

    @Test
    void testError(){
        StepVerifier.create(getUsername(3))
                .expectError()
                .verify();
    }
    /*
		expectError.verify() is equivalent to verifyError()
	 */
    @Test
    void testError1(){
        StepVerifier.create(getUsername(3))
                .verifyError();
    }

    @Test
    void testExceptionClass(){
        StepVerifier.create(getUsername(3))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testExceptionMessage(){
        StepVerifier.create(getUsername(3))
                .expectErrorMessage("oops")
                .verify();
    }

    @Test
    void testBothExceptionClassAndMessage(){
        StepVerifier.create(getUsername(3))
                .consumeErrorWith(ex->{
                    Assertions.assertEquals("oops", ex.getMessage());
                    Assertions.assertEquals(RuntimeException.class, ex.getClass());
                })
                .verify();
    }
}
