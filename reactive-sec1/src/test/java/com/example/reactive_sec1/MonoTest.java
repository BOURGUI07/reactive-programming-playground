package com.example.reactive_sec1;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
class MonoTest {

	private Mono<String> getProduct(int id){
		return Mono.fromSupplier(()-> "product-" + id)
				.doFirst(()-> log.info("INVOKED"));
	}

	@Test
	void test0(){
		StepVerifier.create(getProduct(1))
				.expectNext("product-1")
				.expectComplete() // expect that after emitting product-1, a complete signal
				.verify(); //subscriber
	}
	/*
		expectComplete.verify() is equivalent to verifyComplete()
	 */

	@Test
	void test1(){
		StepVerifier.create(getProduct(1))
				.expectNext("product-1")
				.verifyComplete();
	}

}
