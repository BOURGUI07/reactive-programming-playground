package com.example.reactive_sec1;

import com.example.reactive_sec1.common.ConcreteHttpClient;
import com.example.reactive_sec1.common.Util;
import com.example.reactive_sec1.helper.CountryGenerator;
import com.example.reactive_sec1.helper.NameGenerator;
import com.example.reactive_sec1.publisher.PublisherImpl;
import com.example.reactive_sec1.publisher.SubscriptionImpl;
import com.example.reactive_sec1.subscriber.SubscriberImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.PropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class ReactiveSec1Application {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(ReactiveSec1Application.class, args);
		statefulFull();
	}

	private static void demo1(){
		var publisher = new PublisherImpl();
		var subscriber = new SubscriberImpl();
		publisher.subscribe(subscriber);
	}

	private static void demo2() throws InterruptedException {
		var publisher = new PublisherImpl();
		var subscriber = new SubscriberImpl();
		publisher.subscribe(subscriber);
		var subscription = new SubscriptionImpl(subscriber);
		subscriber.getSubscription().request(3);
		Thread.sleep(Duration.ofSeconds(2));
		subscriber.getSubscription().request(3);
		Thread.sleep(Duration.ofSeconds(2));
		subscriber.getSubscription().request(3);
		Thread.sleep(Duration.ofSeconds(2));
		subscriber.getSubscription().request(3);
	}

	private static void demo3() throws InterruptedException {
		var publisher = new PublisherImpl();
		var subscriber = new SubscriberImpl();
		publisher.subscribe(subscriber);
		var subscription = new SubscriptionImpl(subscriber);
		subscriber.getSubscription().request(3);
		Thread.sleep(Duration.ofSeconds(2));
		subscriber.getSubscription().cancel();
		subscriber.getSubscription().request(3);
		Thread.sleep(Duration.ofSeconds(2));
	}

	private static void demo4() throws InterruptedException {
		var publisher = new PublisherImpl();
		var subscriber = new SubscriberImpl();
		publisher.subscribe(subscriber);
		var subscription = new SubscriptionImpl(subscriber);
		subscriber.getSubscription().request(3);
		Thread.sleep(Duration.ofSeconds(2));
		subscriber.getSubscription().request(11);
		Thread.sleep(Duration.ofSeconds(2));
		subscriber.getSubscription().request(3);
		Thread.sleep(Duration.ofSeconds(2));
	}

	public static List lazyStream(){
		return Stream.of(1).peek(x->log.info("RECEIVED ELEMENT: {}",x)).toList();
	}

	public static void monoJust(){
		var mono = Mono.just("YOUNESS");
		var subscriber = new SubscriberImpl();
		mono.subscribe(subscriber);
		subscriber.getSubscription().request(10);
	}

	public static void monoSubscribeConsumer(){
		var mono = Mono.just(1)
				.map(i->i+2);
		mono.subscribe(
				value -> log.info("RECEIVED ELEMENT: {}",value),
				error-> log.error("ERROR: {}",error.getMessage()),
				() -> log.info("COMPLETED"),
				subscription -> subscription.request(1)
				);
	}

	public static void defaultSub(){
		var mono = Mono.just(1);
		mono.subscribe(Util.subscriber());
		mono.subscribe(Util.subscriber("sub1"));
		mono.subscribe(Util.subscriber("sub2"));
	}

	private static Mono<String> getUsername(int userId){
		return switch (userId){
			case 1 -> Mono.just("YOUNESS");
			case 2 -> Mono.empty();
			default -> Mono.error(new RuntimeException("Invalid user id: " + userId));
		};

		/*
			getUsername(1).subscribe(Util.subscriber());
			getUsername(2).subscribe(Util.subscriber());
			getUsername(3).subscribe(Util.subscriber());
			getUsername(3).subscribe(value -> System.out::println, error -> {});
		 */
	}

	private static void sum(List<Integer> list){
		log.info("STARTING THE PROCESS OF FINDING THE SUM OF LIST: {}",list);
		var sum = list.stream().mapToInt(q->q).sum();
		Mono.just(sum); //it will start the process regardless of whether a sub exists or not
		// Mono.fromSupplier(()->sum); // unless there's a sub, it won't start the process
		// Mono.fromSupplier(()-> sum)
		//	.subscribe(sumValue -> log.info("THE SUM OF LIST IS: {}",sumValue));
		// Mono.fromCallable(()-> sum)
		//	.subscribe(sumValue -> log.info("THE SUM OF LIST IS: {}",sumValue));
		/*
			Callable and Supplier are the same,
			if your method throws an Exception you should use fromCallable(),
			of if you wanna use fromSupplier(), you must handle exceptions
			otherwise, you're fine with fromSupplier in the absence of thrown exceptions.
		 */

	}

	public static void getProductName(int productId){
		Mono<String> mono = productId==1?
				Mono.fromSupplier(()->Util.faker().commerce().productName())
				:Mono.fromRunnable(()-> notifyBusiness(productId));
				mono.subscribe(productName -> log.info("PRODUCT NAME: {}",productName));

	}

	public static void notifyBusiness(int productId){
		log.info("NOTIFYING BUSINESS ON AN UNAVAILABLE PRODUCT ID: {}",productId);
	}

	public static void getName(){
		CompletableFuture<String> name = CompletableFuture.supplyAsync(()-> {
			log.info("GENERATING NAME");
			return Util.faker().name().firstName();
		});
		// from Future method is eager by default, it will start the process even in the abesence of a sub
		// but it won't generate the value unless you sleep the thread.
		Mono.fromFuture(name); // EAGER, even if there's no sub, it will start the process of generating the name
		Util.sleepThread(1);

		Mono.fromFuture(()->name) // LAZY, Unless you give a sub, it won't start the process of generating name
				.subscribe(firstName -> log.info("FIRST NAME IS: {}",firstName));
		Util.sleepThread(1);
	}

	/*
		Both the Mono.just() and Mono.fromSupplier() CREATE the publisher right away
		difference is, the 1st EXECUTES it right away and the 2nd delays it until
		there's a sub.
	 */

	public static Mono<Integer> createPublisher(List<Integer> list){
		log.info("STARTING THE PROCESS OF GENERATING SUM OF LIST: {}",list);
		Util.sleepThread(2);
		var sum = list.stream().mapToInt(q->q).sum();
		Util.sleepThread(3);
		return  Mono.fromSupplier(()->sum);
	}

	public static void delayPubblish(){
		Mono.defer(()->createPublisher(List.of(0,1,2,3)))
				.subscribe(Util.subscriber("YOUNESS"));

		/*
			Mono.defer() won't CREATE a publisher unless there's a SUB!
		 */
	}

	public static void consumeProductResponse(int id){
		new ConcreteHttpClient().getProductName(id)
				.subscribe(Util.subscriber("YOUNESS"));
		Util.sleepThread(2);
	}

	public static void fluxJust(){
		Flux.just(1,4,"sam","$14.15").subscribe(Util.subscriber());
	}

	public static void fluxMultipleSubs(){
		var flux = Flux.just(1,2,3,4,5);
		flux.subscribe(Util.subscriber());
		flux
				.filter(i->i%2!=0)
				.subscribe(Util.subscriber("John"));
		flux.filter(i->i%2==0).subscribe(Util.subscriber("Mark"));
		flux.map(x->x+"_A").subscribe(Util.subscriber("Bob"));

	}

	public static void fluxFromIterable(){
		var list = List.of("a","bc","dec","acd");
		var flux = Flux.fromIterable(list);
		flux.filter(x-> x.startsWith("a"))
				.subscribe(Util.subscriber());
	}

	public static void fluxFromStream(){
		var stream = Stream.of("a","c","d","b");
		var flux = Flux.fromStream(stream);
		flux.subscribe(Util.subscriber());
		// flux.subscribe(Util.subscriber("sub1")); won't receive elements!
		// the stream has been already been operated upon
		// you can't use the stream again after consuming first time
		// if you have a stream, and you wanna multiple subs, then you have to use supplier of stream
		var list = List.of("x","y","z");
		var flux1 = Flux.fromStream(()->list.stream());
		flux1.subscribe(Util.subscriber());
		flux1.subscribe(Util.subscriber("John"));
		/*
			if you have used,
			var stream1 = Stream.of("x","y","z");
			var flux1 = Flux.fromStream(()->stream1);
			and you wanna use multiple subs, then the John gonna cause an error!!
		 */
	}

	public static void fluxRange(){
		var flux = Flux.range(1,10); // from 1 to 10
		flux.subscribe(Util.subscriber());
		var flux2 = Flux.range(3,10); // from 3 to 12
		flux2.subscribe(Util.subscriber());
		flux.map(i-> Util.faker().name().firstName()).subscribe(Util.subscriber());
	}

	public static void fluxLogger(){
		var flux = Flux.range(1,5);
		flux.log().subscribe(Util.subscriber());
		/*
			log() takes the values from flux and gives them to last sub
			log() here is both a sub and pub
		 */
		flux.log().map(t-> Util.faker().name().firstName()).subscribe(Util.subscriber());
	}

	public static void listVsFlux(){
		System.out.println(NameGenerator.nameList(10));
		// it will print the names after 10 secs
		// a second for each name
		// it will block as long as it didn't generate the complete list
		// either 10 names or NOTHING

		NameGenerator.nameFlux(10).subscribe(Util.subscriber());
		// as long as an element is generated, it will print it!
		// it won't block!
		// I can request as much as I can and I can cancel whenever I want
		var sub = new SubscriberImpl();
		NameGenerator.nameFlux(10).subscribe(sub);
		sub.getSubscription().request(3);
	}

	public static void fluxInterval(){
		var flux = Flux.interval(Duration.ofMillis(500));
				flux.subscribe(Util.subscriber());
				Util.sleepThread(2);
		/*
			the above will print numbers from 0,1,2... after every 500 ms
			till the thread sleeps after 2 secs
			if you wanna generate random names every 500ms, then use the below
		 */
		flux.map(i->Util.faker().name().firstName()).subscribe(Util.subscriber());
		Util.sleepThread(10);

	}

	public static void fluxEmptyError(){
		Flux.empty().subscribe(Util.subscriber());
		Flux.error(new RuntimeException("")).subscribe(Util.subscriber());
	}

	public static void fluxDefer(){
		Flux.defer(() -> Flux.fromIterable(List.of(1,2,3))).subscribe(Util.subscriber());
		// it won't CREATE the pub unless there is a sub!
	}

	public static void fromFluxToMono(){
		var flux = Flux.just(1,2,3);
		var mono = Mono.from(flux);

	}
	public static void fromMonoToFlux(){
		var mono = Mono.just(8);
		var flux = Flux.from(mono);
	}

	public static void fluxCreate(){
		Flux.create(fluxSink -> {
			fluxSink.next(1);
			fluxSink.next(2);
			fluxSink.complete();
		}).subscribe(Util.subscriber("SUB"));
	}

	public static void fluxCreate1(){
		Flux.create(fluxSink -> {
			String country;
			do {
				country=Util.faker().country().name();
				fluxSink.next(country);
			}while(!country.equalsIgnoreCase("morocco"));
			fluxSink.complete();
		})
				.subscribe(Util.subscriber("SUB1"));
	}

	public static void fluxCreate2(){
		var generator = new CountryGenerator();
		Flux.create(generator).subscribe(Util.subscriber());
		for(int i=0;i<10;i++){
			generator.generateCounbtry();
		}

	}

	public static void listThreadUnsafety(){
		var list = new ArrayList<Integer>();
		Runnable runnable = ()->{
			for(int i =0; i<1000;i++){
				list.add(i);
			}
		};
		for(int i=0;i<10;i++){
			Thread.ofPlatform().start(runnable);
		}
		log.info("THE FINAL SIZE OF LIST: {}",list.size());
		/*
			The list size supposed to be 10000 items.
			We have 10 threads.
			Each thread gonna inject 1000 items into the list
			Then the final size of list is only: 1641 items
			That's why the list is thread-unsafe!!
		 */
	}

	public static void fluxSinkThreadSafety(){
		var list = new ArrayList<String>();
		var generator = new CountryGenerator();
		var flux = Flux.create(generator);
		flux.subscribe(list::add);

		Runnable runnable = ()->{
			for(int i =0; i<1000;i++){
				generator.generateCounbtry();
			}
		};
		for(int i=0;i<10;i++){
			Thread.ofPlatform().start(runnable);
		}
		Util.sleepThread(3);
		log.info("THE FINAL SIZE OF LIST: {}",list.size());

		/*
			The final list size is 10000 just as expected!
			flux is thread safe
		 */
	}

	public static void defaultFluxCreateBehavior(){
		var sub = new SubscriberImpl();
		Flux.<String>create(fluxSink -> {
			for(int i=0;i<10;i++){
				var name = Util.faker().country().name();
				log.info("GENERATING NAME: {}",name);
				fluxSink.next(name);
			}
			fluxSink.complete();
		}).subscribe(sub);

		Util.sleepThread(3);
		sub.getSubscription().request(2);
		Util.sleepThread(3);
		sub.getSubscription().request(2);
		sub.getSubscription().cancel();

		// it generates the names first and store them in a queue
		// and then if a sub requested them, the pub forwards them to him
		// the producer doesn't wait for the sub to request
		// it will store the generated data in a queue until the sub requests
		// if you don't want to produce early, product on demand, see next method
	}

	public static void productOnDemand(){
		// The producer won't generate data until the sub requests it!!

		var sub = new SubscriberImpl();
		Flux.<String>create(fluxSink -> {
			fluxSink.onRequest(request -> {
				for(int i=0;i<request && !fluxSink.isCancelled();i++){
					var name = Util.faker().country().name();
					log.info("GENERATING NAME: {}",name);
					fluxSink.next(name);
				}
			});
		}).subscribe(sub);

		Util.sleepThread(2);
		sub.getSubscription().request(2);
		Util.sleepThread(2);
		sub.getSubscription().request(2);
		sub.getSubscription().cancel();

	}

	public static void takeOperator(){
		IntStream.range(1,10)
				.limit(3)
				.forEach(System.out::println)
		;
		Flux.range(1,10)
				.take(3)
				.subscribe(System.out::println);

		Flux.range(1,10)
				.takeWhile(x->x<4) // stop when the condition isn't met
				.subscribe(System.out::println);

		Flux.range(1,10)
				.takeUntil(x->x==3) // stop when the condition is met
									// it will at least emit ONE item
				.subscribe(System.out::println);
	}

	public static void synchronousSink(){
		Flux.generate(synchronousSink -> {
			synchronousSink.next(1);
			synchronousSink.complete();// without complete(), it will keep emitting value of 1 endlessly
		}).subscribe(Util.subscriber());

		// It will emit one value at max


		// It will emit value '1', three times!
		Flux.generate(synchronousSink -> {
			synchronousSink.next(1);
		}).take(3).subscribe(Util.subscriber());

		Flux.generate(synchronousSink -> {
			synchronousSink.next(Util.faker().country().name());
		}).take(3).subscribe(Util.subscriber());


		// It will stop when the complete() is invoked
		Flux.generate(synchronousSink -> {
			synchronousSink.next(Util.faker().country().name());
			synchronousSink.complete();
		}).take(3).subscribe(Util.subscriber());


		//It will stop when an error is invoked
		Flux.generate(synchronousSink -> {
			synchronousSink.next(Util.faker().country().name());
			synchronousSink.error(new RuntimeException("Exception"));
		}).take(3).subscribe(Util.subscriber());




		Flux.<String>generate(synchronousSink -> {
			/*
				here the flux generate is stateless
				it generates a new value of country, whenevere the stream starts
				if you wanna maintain the state
				see next method
			 */
			String country = Util.faker().country().name();
			synchronousSink.next(country);
		}).takeUntil(x-> x.equalsIgnoreCase("canada"))
				.subscribe(Util.subscriber());
	}

	public static void statefulFull(){
		/*
			requirement: emit 10 countries, stop when the counter reacher 10
			or the country name is canada
		 */
		Flux.generate(
				()->0,
				(counter, sink) ->{
					var country = Util.faker().country().name();
					sink.next(country);
					counter++;
					if(counter==10 || country.equalsIgnoreCase("morocco")){
						sink.complete();
					}
					return counter;
				}
		).subscribe(Util.subscriber());
	}
}
