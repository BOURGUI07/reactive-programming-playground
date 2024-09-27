package com.example.reactive_sec1;

import com.example.reactive_sec1.assignments.assignment1.FileReaderServiceImpl;
import com.example.reactive_sec1.assignments.assignment5.vinoth.BookService;
import com.example.reactive_sec1.common.ConcreteHttpClient;
import com.example.reactive_sec1.common.Util;
import com.example.reactive_sec1.examples.flatmap.OrderService;
import com.example.reactive_sec1.examples.flatmap.PaymentService;
import com.example.reactive_sec1.examples.flatmap.User;
import com.example.reactive_sec1.examples.flatmap.UserService;
import com.example.reactive_sec1.examples.merge.Emirates;
import com.example.reactive_sec1.examples.merge.Qatar;
import com.example.reactive_sec1.examples.merge.Ryanair;
import com.example.reactive_sec1.helper.CountryGenerator;
import com.example.reactive_sec1.helper.NameGenerator;
import com.example.reactive_sec1.helper.NameGenerator1;
import com.example.reactive_sec1.publisher.PublisherImpl;
import com.example.reactive_sec1.publisher.SubscriptionImpl;
import com.example.reactive_sec1.subscriber.SubscriberImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class ReactiveSec1Application {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(ReactiveSec1Application.class, args);
		sinkMany_multicast();
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
				it generates a new value of country, whenever the stream starts
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
			requirement: emit 10 countries, stop when the counter reaches 10
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

	public static void assignment1(){
		var path = Path.of("C:\\Users\\hp\\Documents\\projects\\reactive\\reactive-sec1\\file.txt");
		var readerService = new FileReaderServiceImpl();
		readerService.read(path)
				//.take(8)
				.takeUntil(s->s.equalsIgnoreCase("line8"))
				.subscribe(Util.subscriber());

	}

	public static void handle(){
		/*
			if number is 1, then send 2
			if number is 4, don't send it
			if number is 7, error
		 */

		var flux = Flux.range(1,10)
				.filter(x->x!=7);
		var flux1 = flux.handle((item,sink) ->{
			switch (item){
				case 1 -> sink.next(-2);
				case 4 -> {}
				case 7 -> sink.error(new RuntimeException("Exception"));
				default -> sink.next(item);
			}
		});
		flux1.subscribe(Util.subscriber());
	}

	public static void handle2(){
		var flux = Flux.<String>generate(synchronousSink -> {
			String country = Util.faker().country().name();
			synchronousSink.next(country);
		});
		var flux1 = flux.handle((country,sink) ->{
			sink.next(country);
			if(country.equalsIgnoreCase("canada")){
				sink.complete();
			}
		});
		flux1.subscribe(Util.subscriber());
	}

	public static void doHooksAndCallbacks(){
		Flux.<Integer>create(fluxSink -> {
					log.info("producer begins");
					for (int i = 0; i < 4; i++) {
						fluxSink.next(i);
					}
					fluxSink.complete();
					// fluxSink.error(new RuntimeException("oops"));
					log.info("producer ends");
				})
				.doOnComplete(() -> log.info("doOnComplete-1"))
				.doFirst(() -> log.info("doFirst-1"))
				.doOnNext(item -> log.info("doOnNext-1: {}", item))
				.doOnSubscribe(subscription -> log.info("doOnSubscribe-1: {}", subscription))
				.doOnRequest(request -> log.info("doOnRequest-1: {}", request))
				.doOnError(error -> log.info("doOnError-1: {}", error.getMessage()))
				.doOnTerminate(() -> log.info("doOnTerminate-1")) // complete or error case
				.doOnCancel(() -> log.info("doOnCancel-1"))
				.doOnDiscard(Object.class, o -> log.info("doOnDiscard-1: {}", o))
				.doFinally(signal -> log.info("doFinally-1: {}", signal)) // finally irrespective of the reason
				// .take(2)
				.doOnComplete(() -> log.info("doOnComplete-2"))
				.doFirst(() -> log.info("doFirst-2"))
				.doOnNext(item -> log.info("doOnNext-2: {}", item))
				.doOnSubscribe(subscription -> log.info("doOnSubscribe-2: {}", subscription))
				.doOnRequest(request -> log.info("doOnRequest-2: {}", request))
				.doOnError(error -> log.info("doOnError-2: {}", error.getMessage()))
				.doOnTerminate(() -> log.info("doOnTerminate-2")) // complete or error case
				.doOnCancel(() -> log.info("doOnCancel-2"))
				.doOnDiscard(Object.class, o -> log.info("doOnDiscard-2: {}", o))
				.doFinally(signal -> log.info("doFinally-2: {}", signal)) // finally irrespective of the reason
				//.take(4)
				.subscribe(Util.subscriber("subscriber"));
	}

	public static void delay(){
		Flux.range(1,10)
				.delayElements(Duration.ofSeconds(1))
				.subscribe(Util.subscriber());
		Util.sleepThread(12);
	}

	public static void subscribe(){
		Flux.range(1,10)
				.doOnNext(x-> log.info("RECIEVED ITEM: {}",x))
				.doOnComplete(()-> log.info("COMPLETED"))
				.doOnError(error-> log.info("ERROR: {}", error.getMessage()))
				.subscribe();
	}

	public static void onErrorReturn(){
		Flux.range(1,10)
				.map(x-> x==5? x/0:x)
			//	.onErrorReturn(-1) // in case of error, return -1
				.onErrorReturn(ArithmeticException.class,-1)// in case of exception specified, return -1
				.subscribe(Util.subscriber());
	}

	public static void onErrorResume(){
		Mono.error(new RuntimeException("Exception"))
				.onErrorResume(ArithmeticException.class,ex->fallBack()) // in case of error caused specifically by that exception, call the fallback() methdo
				.onErrorResume(ex-> fallBack1()) // if any kind of error happens, call the fallback1() method
				.onErrorReturn(-2) // if fallback2() fails, then it will return -2
				.subscribe(Util.subscriber());

		// fallback service

	}

	public static Mono<Integer> fallBack(){
		return Mono.fromSupplier(() -> Util.faker().random().nextInt(6,100));
	}

	public static Mono<Integer> fallBack1(){
		return Mono.error(new RuntimeException("Exception"));
	}

	public static void onErrorComplete(){
		// Either give me the value or complete, don't ever give me an error!
		Mono.error(new RuntimeException("Exception"))
				.onErrorComplete()
				.subscribe(Util.subscriber());
	}

	public static void onErrorContinue(){
		/*
			In case, you wanna proceed onto the next emitted items,
			even in the presence of error.
			Skip the error and proceed further
		 */

		Flux.range(1,10)
				.map(x-> x==5? x/0:x)
				.onErrorContinue((ex,obj)->log.info("ERROR :{} CAUSED BY ITEM: {}",ex.getMessage(),obj))
				.subscribe(Util.subscriber());
	}

	public static void defaultIfEmpty(){
		Mono.empty()
				.defaultIfEmpty(4)
				.subscribe(Util.subscriber());

		Flux.range(1,10)
				.filter(x-> x>11)
				.defaultIfEmpty(4)
				.subscribe(Util.subscriber());
	}

	public static void switchIfEmpty(){
		Flux.range(1,10)
				.filter(x-> x>11)
				.switchIfEmpty(fallBack4())
				.subscribe(Util.subscriber());

	}

	public static Flux<Integer> fallBack4(){
		return Flux.range(100,3);
	}

	public static void timeout(){
		getProductName()
				.timeout(Duration.ofSeconds(1))
				.onErrorReturn("Fallback Value")
				.subscribe(Util.subscriber());
		Util.sleepThread(5);


		getProductName()
		.timeout(Duration.ofSeconds(1),fallBack5())
				.subscribe(Util.subscriber());
	}

	public static Mono<String> getProductName(){
		return Mono.fromSupplier(()-> "service: "  +Util.faker().commerce().productName())
				.delayElement(Duration.ofSeconds(3));
	}

	public static Mono<String> fallBack5(){
		return Mono.fromSupplier(()-> "fallback service: "  +Util.faker().commerce().productName())
				.delayElement(Duration.ofSeconds(3));
	}

	public static void multipleTimeouts(){
		var mono = getProductName().timeout(Duration.ofSeconds(1),fallBack5());

		mono.timeout(Duration.ofMillis(200),fallBack5())
				.subscribe(Util.subscriber());
		/*
		 	The closest timeout to the subscriber is the one that's gonna work!
		 	The value of the timeout closer to the sub has always to be less than
		 	the duration of timeout closer to the producer
		 */
	}

	record Customer(int id, String name){}
	record Order(String productName, int price, int quantity){}

	public static Flux<Customer> customerFlux(){
		return Flux.range(1,3)
				.map(x-> new Customer(x,Util.faker().name().fullName()));
	}

	public static Flux<Order> orderFlux(){
		return Flux.range(1,3)
				.map(x-> new Order(Util.faker().commerce().productName(),Integer.parseInt(Util.faker().commerce().price(9,100)),x));
	}

	public static void withoutTransform(){
		customerFlux()
				.doOnNext(customer-> log.info("CUSTOMER WITH ID: {}", customer.id))
				.doOnComplete(() -> log.info("COMPLETED"))
				.doOnError(error -> log.info("ERROR: {}", error.getMessage()))
				.subscribe(Util.subscriber());

		orderFlux()
				.doOnNext(order-> log.info("ORDER WITH PRODUCT NAME: {}", order.productName))
				.doOnComplete(() -> log.info("COMPLETED"))
				.doOnError(error -> log.info("ERROR: {}", error.getMessage()))
				.subscribe(Util.subscriber());

		/*
			Both pipeline share the same doOnComplete() and doOnError() logic (debugger helpers)
			see how to write reusable reactive logic for both pipelines in the next method
		 */
	}

	private static <T> UnaryOperator<Flux<T>> addDebugger(){
			return flux -> flux
					.doOnComplete(() -> log.info("COMPLETED"))
					.doOnError(error -> log.info("ERROR: {}", error.getMessage()));
	}

	public static void withTransform(){
		customerFlux()
				.doOnNext(customer-> log.info("CUSTOMER WITH ID: {}", customer.id))
				.transform(addDebugger())
				.subscribe(Util.subscriber());

		orderFlux()
				.doOnNext(order-> log.info("ORDER WITH PRODUCT NAME: {}", order.productName))
				.transform(addDebugger())
				.subscribe(Util.subscriber());
	}


	public static void withTransform1(){
		var debuggerEnabled = false;
		customerFlux()
				.doOnNext(customer-> log.info("CUSTOMER WITH ID: {}", customer.id))
				.transform(debuggerEnabled?addDebugger(): Function.identity()) // if debugger enabled, then add it, else, return flux as it is
				.subscribe(Util.subscriber());
	}


	public static void moviestream(){
		Flux<String> flux = Flux.generate(
				() -> {
					log.info("RECIEVED REQUEST");
					return 1;
				},
				(state,sink)-> {
					var scene = "MOVIE SCENE " + state;
					log.info("PLAYING SCENE: {}", scene);
					sink.next(scene);
					return ++state;
				}
		).take(10).delayElements(Duration.ofSeconds(1))
				.cast(String.class)
				.share(); // MAKE THE PUBLISHER HOT
			//  .publish().refCount(1) it needs at least 1 sub to emit the data
			//  .publish().autoConnect()  The movie won't start unless a sub joins the theater,when both sub1 and sub2 leave, the movie gonna keep playing.
			//  .publish().autoConnect(0) The movie neither will wait for subs to join nor will stop playing after they leave
		Util.sleepThread(2);
		flux.subscribe(Util.subscriber("SUB1"));

		Util.sleepThread(3);
		flux
				.take(3)
				//Now SUB2 is going to watch only 3 scenes,
				// that won't affect SUB1 number of requests
				// As long there's a sub, the movie gonna keep playing
				// if publish()refCount() was set to 2,
				//that is, it's needed at least 2 subs to emit the data
				// the publisher gonna wait UNTIL sub2 joins to start emitting data
				//the moment any sub leaves between these two, the publisher
				// gonna stop generating data
				.subscribe(Util.subscriber("SUB2"));

		Util.sleepThread(15);

		/*
			SUB1 will receive the request, then watch 3 scenes
			That is, 3 seconds, passed. SUB2 will join and the
			publisher receive the request
			By the time SUB1 is watching scene 4, SUB2 is watching
			scene 2.
			They're like netflix. COLD PUBLISHER
			To make it like a movie theater
			That is, whenever SUB2 joins, he will be watching the same
			scene as SUB1.
			To do that add .share() to the flux.

		 */
	}

	public static void stockStream(){
		var flux = Flux.generate(synchronousSink -> {
			synchronousSink.next(Util.faker().random().nextInt(10,100));
		}).delayElements(Duration.ofSeconds(3))
				.doOnNext(price->log.info("EMITTING PRICE: {}", price))
				.publish().autoConnect(0);
			  //.replay(10).autoConnect(0);

		Util.sleepThread(4);
		log.info("SAM JOINING");
		flux.subscribe(Util.subscriber("SAM"));

		Util.sleepThread(4);
		log.info("MIKE JOINING");
		flux.subscribe(Util.subscriber("MIKE"));

		Util.sleepThread(15);

		/*
			3 secs passed, and first price emitted
			1 sec after, SAM joins
			SAM won't be able to receive the past price
			He won't be able to know the CURRENT price
			until 2 secs passes, then he will receive his 1st price
			To make SUM see the current or past scores the moment he joins
			Replace publish().autoConnect(0) with replay().autoConnect(0)
			if You wanna make sure SAM knows for example the last 10 price values
			then replay(10).autoConnect(0)
		 */


	}


	public static void defaultBehavior(){
		var flux = Flux.create(sink->{
			for(int i=0;i<2;i++){
				log.info("GENERATING: {}", i);
				sink.next(i);
			}
			sink.complete();
		})
				.doOnNext(value-> log.info("VALUE: {}", value));

		flux.subscribe(Util.subscriber());
		/*
			When you run the above program, everything will be done by
			the MAIN Thread.
		 */
		Runnable runnable = () -> flux.subscribe(Util.subscriber());
		Thread.ofPlatform().start(runnable);

		/*
			Now when you run the above program, everything will be generated by
			The new Thread (Thread-0).

			Whoever subscribes to the publisher is going to do all the work.
		 */
	}

	public static void subscribeOn(){
		var flux = Flux.create(sink->{
					for(int i=0;i<2;i++){
						log.info("GENERATING: {}", i);
						sink.next(i);
					}
					sink.complete();
				})
				.doOnNext(value-> log.info("VALUE: {}", value));

		flux.doFirst(()-> log.info("FIRST1"))
				.subscribeOn(Schedulers.boundedElastic())
				.doFirst(()-> log.info("FIRST2"))
				.subscribe(Util.subscriber());
		Util.sleepThread(2); // blocking the main thread
		/*
			When you run the above program, 'FIRST2' gonna be printed
			by the main thread, then boundedElastic-0 will do the rest work
		 */

		Runnable runnable = () -> flux.doFirst(()-> log.info("FIRST1"))
				.subscribeOn(Schedulers.boundedElastic())
				.doFirst(()-> log.info("FIRST2"))
				.subscribe(Util.subscriber());
		/*
			When you run the above program, 'FIRST2' gonna be printed
			by the new thread (thread-0), then boundedElastic-0 will do the rest work
		 */
	}

	public static void multipleSubcribeOn(){
		var flux = Flux.create(sink->{
					for(int i=0;i<2;i++){
						log.info("GENERATING: {}", i);
						sink.next(i);
					}
					sink.complete();
				})
				.subscribeOn(Schedulers.newParallel("vins"))
				.doOnNext(value-> log.info("VALUE: {}", value))
				.doFirst(()-> log.info("FIRST1"))
				.subscribeOn(Schedulers.boundedElastic())
				.doFirst(()-> log.info("FIRST2"));

		Runnable runnable = () -> flux.subscribe(Util.subscriber());
		Thread.ofPlatform().start(runnable);
		Util.sleepThread(2);

		/*
			when you run the above program, "FIRST2" gonna be printed By Thread-0
			"FIRST1" gonna be printed by boundedElastic-1.
			the rest of work gonna be handled by vins-1 thread.
			The closest Thread to the producer gonna end up doing all the work
		 */
	}

	public static void virtualThread(){
		var flux = Flux.create(sink->{
					for(int i=0;i<2;i++){
						log.info("GENERATING: {}", i);
						sink.next(i);
					}
					sink.complete();
				})
				.doOnNext(value-> log.info("VALUE: {}", value))
				.doFirst(()-> log.info("FIRST1 {}", Thread.currentThread().isVirtual())) // false
				.subscribeOn(Schedulers.boundedElastic())
				.doFirst(()-> log.info("FIRST2"));

		Runnable runnable = () -> flux.subscribe(Util.subscriber());
		Thread.ofPlatform().start(runnable);
		Util.sleepThread(2);
		/*
			If wanna set the property of vitrual thread
		 */
		System.getProperty("reactor.Schedulers.defaultBoundedElasticOnVirtualThreads","true");
	}

	private static void publishOn(){
		var flux = Flux.create(sink->{
					for(int i=0;i<2;i++){
						log.info("GENERATING: {}", i);
						sink.next(i);
					}
					sink.complete();
				})
				.doOnNext(value-> log.info("VALUE: {}", value))
				.doFirst(()-> log.info("FIRST1"))
				.publishOn(Schedulers.boundedElastic())
				.doFirst(()-> log.info("FIRST2"));

		Runnable runnable = () -> flux.subscribe(Util.subscriber());
		Thread.ofPlatform().start(runnable);
		Util.sleepThread(2);
	}

	public void parallel(){
		Flux.range(1,5)
				.parallel()
				.runOn(Schedulers.boundedElastic())
				.map(this::process) // gonna be processed in parallel
				.sequential()
				.map(x->x+"a")// gonna be processed sequentially
				.subscribe(Util.subscriber());

		Util.sleepThread(2);//blocking main thread
		/*
			It will command another thread for processing
			this way, the result gonna be reached faster
		 */
	}

	private  int process(int i){
		log.info("TIME CONSUMING TASK {}",i);
		Util.sleepThread(1);
		return i*2;
	}

	public static void automaticBackPressure(){
		var producer = Flux.generate(
				() ->1,
				(state,sink)->{
					log.info("GENERATING: {}",state);
					sink.next(state);
					return ++state;
				}
		).cast(Integer.class);

		producer.publishOn(Schedulers.boundedElastic())
				.map(ReactiveSec1Application::timeConsumingTask)
				.subscribe(Util.subscriber());
		Util.sleepThread(60);

		/*
			The main thread gonna stop producing items at item 256
			Then he will give responsibility to the other thread to
			give the subscriber the emitted items
			The publisher after emitting 256 items, gonna wait for the consumer
			to slowly catch-up(1 sec to map an item).
		 */
	}

	private static int timeConsumingTask(int i){
		Util.sleepThread(1);
		return i;
	}

	public static void automaticBackPressure2(){
		var producer = Flux.generate(
				() ->1,
				(state,sink)->{
					log.info("GENERATING: {}",state);
					sink.next(state);
					return ++state;
				}
		).cast(Integer.class)
				.subscribeOn(Schedulers.parallel());

		producer.publishOn(Schedulers.boundedElastic())
				.map(ReactiveSec1Application::timeConsumingTask)
				.subscribe(Util.subscriber());
		Util.sleepThread(60);

		/*
			The parallel thread gonna stop producing items at item 256
			Then he will give responsibility to the other thread(boundedElastic) to
			give the subscriber the emitted items
			The publisher after emitting 256 items, gonna wait for the consumer
			to slowly catch-up(1 sec to map an item).
			If you want the producer to produce, for example, 16 items then passes
			the responsibility to the other thread, add the following statement in the main method
			System.setProperty("reactor.bufferSize.small","16");
			The producer after producing 16 items, will stop.
			After consuming 75% of the items by the consumer, the producer will start
			production process again.
			So after consuming item number 12 by the consumer, the producer gonna start
			producing items starting from number 17.
			After generating 16 items again, that is, 28 items, the consumer gonna start
			consuming items starting with item number 13.
		 */

	}

	public static void limitRate(){
		var producer = Flux.generate(
						() ->1,
						(state,sink)->{
							log.info("GENERATING: {}",state);
							sink.next(state);
							return ++state;
						}
				).cast(Integer.class)
				.subscribeOn(Schedulers.parallel());

		producer.limitRate(5) //we tell the producer don't produce more than 5 items before I consume
				.publishOn(Schedulers.boundedElastic())
				.map(ReactiveSec1Application::timeConsumingTask)
				.subscribe(Util.subscriber());
		Util.sleepThread(60);

		/*
			The producer after producing 5 items, will stop.
			After consuming 75% of the items by the consumer, the producer will start
			production process again.
			So after consuming item number 3 by the consumer, the producer gonna start
			producing items starting from number 6.
		 */
	}

	public static void backPressureMultipleSubs(){
		var producer = Flux.generate(
						() ->1,
						(state,sink)->{
							log.info("GENERATING: {}",state);
							sink.next(state);
							return ++state;
						}
				).cast(Integer.class)
				.subscribeOn(Schedulers.parallel());

		producer.limitRate(5)
				.publishOn(Schedulers.boundedElastic())
				.map(ReactiveSec1Application::timeConsumingTask)
				.subscribe(Util.subscriber("sub1"));

		producer
				.take(50)
				.publishOn(Schedulers.boundedElastic())
				.subscribe(Util.subscriber("sub2"));

		Util.sleepThread(60);

		/*
			The sub2 will receive his items as fast as the producer at producing them
			After that, the sub1 gonna start receiving his items starting from 1.
			The moment he reaches 3, the producer gonna start producing again
		 */
	}

	public static void fluxCreateBackPressureHandling(){
		var producer = Flux.create(fluxSink -> {
			for(int i=1;i<=500 && !fluxSink.isCancelled();i++){
				log.info("GENERATING: {}", i);
				fluxSink.next(i);
				Util.sleepThreadMillis(50);
			}
			fluxSink.complete();
		}).cast(Integer.class).subscribeOn(Schedulers.parallel());

		producer
				.publishOn(Schedulers.boundedElastic())
				.map(ReactiveSec1Application::timeConsumingTask1)
				.subscribe();

		Util.sleepThread(60);

		/*
			producer speed is 20 items per second
			consumer processing speed is 1 item per second

		 */
	}

	private static int timeConsumingTask1(int i){
		log.info("RECEIVED ITEM: {}",i);
		Util.sleepThread(1);
		return i;
	}

	public static void bufferStrategy(){
		var producer = Flux.create(fluxSink -> {
			for(int i=1;i<=500 && !fluxSink.isCancelled();i++){
				log.info("GENERATING: {}", i);
				fluxSink.next(i);
				Util.sleepThreadMillis(50);
			}
			fluxSink.complete();
		}).cast(Integer.class).subscribeOn(Schedulers.parallel());

		producer
				.onBackpressureBuffer() // now the consumer will be able to receive an item per second
				.publishOn(Schedulers.boundedElastic())
				.map(ReactiveSec1Application::timeConsumingTask1)
				.subscribe();

		Util.sleepThread(60);
	}

	public static void errorStrategy(){
		var producer = Flux.create(fluxSink -> {
			for(int i=1;i<=500 && !fluxSink.isCancelled();i++){
				log.info("GENERATING: {}", i);
				fluxSink.next(i);
				Util.sleepThreadMillis(50);
			}
			fluxSink.complete();
		}).cast(Integer.class).subscribeOn(Schedulers.parallel());

		producer
				.onBackpressureError() // throw an error when consumer is too slow to catch up
				.publishOn(Schedulers.boundedElastic())
				.map(ReactiveSec1Application::timeConsumingTask1)
				.subscribe();

		Util.sleepThread(60);
	}

	public static void fixedBufferSizeStrategy(){
		var producer = Flux.create(fluxSink -> {
			for(int i=1;i<=500 && !fluxSink.isCancelled();i++){
				log.info("GENERATING: {}", i);
				fluxSink.next(i);
				Util.sleepThreadMillis(50);
			}
			fluxSink.complete();
		}).cast(Integer.class).subscribeOn(Schedulers.parallel());

		producer
				.onBackpressureBuffer(10) // if the producer generated more items than bufferSize+number of items consumer, it will throw and error
				.publishOn(Schedulers.boundedElastic())
				.map(ReactiveSec1Application::timeConsumingTask1)
				.subscribe();

		Util.sleepThread(60);
	}

	public static void dropStrategy(){
		var producer = Flux.create(fluxSink -> {
			for(int i=1;i<=500 && !fluxSink.isCancelled();i++){
				log.info("GENERATING: {}", i);
				fluxSink.next(i);
				Util.sleepThreadMillis(50);
			}
			fluxSink.complete();
		}).cast(Integer.class).subscribeOn(Schedulers.parallel());

		producer
				//.onBackpressureDrop()
				.onBackpressureLatest()
				.publishOn(Schedulers.boundedElastic())
				.map(ReactiveSec1Application::timeConsumingTask1)
				.subscribe();

		Util.sleepThread(60);

		/*
			If the consumer requested 2 items, and the producer produced 20 items
			the monitor gonna take 2 items from the producer
			and drop the remaining items(18)
		 */
		/*
			with lastest strategy,
			If the consumer requested 2 items, and the producer produced 20 items
			the monitor gonna take 3 items from the producer
			 and gonna keep the lastest item
			and drop the remaining items(17)
			so instead of giving item number 2 next time, it will give him item number 19
		 */
	}

	public static void oneStrategyForAllSubs(){
		var producer = Flux.create(fluxSink -> {
			for(int i=1;i<=500 && !fluxSink.isCancelled();i++){
				log.info("GENERATING: {}", i);
				fluxSink.next(i);
				Util.sleepThreadMillis(50);
			}
			fluxSink.complete();
		}, FluxSink.OverflowStrategy.BUFFER)//now bufferStrategy gonna be shared among all subs.
				.cast(Integer.class)
				.subscribeOn(Schedulers.parallel());

		producer
				//.onBackpressureDrop()
				.onBackpressureLatest()
				.publishOn(Schedulers.boundedElastic())
				.map(ReactiveSec1Application::timeConsumingTask1)
				.subscribe();

		Util.sleepThread(60);
	}

	public static void startWith(){
		var producer1 = Flux.just(1,2,3)
				.doOnSubscribe(s-> log.info("SUBSCRIBING TO PRODUCER-1"))
				.delayElements(Duration.ofMillis(20));

		producer1
				.startWith(0,-1)
				.subscribe(Util.subscriber());

		Util.sleepThread(2);

		/*
			The startWith() operator gonna emit 0 and -1 before emitting producer1 values
		 */

		producer1
				.startWith(0,-1)
				.take(2) // only 0 and -1 gonna be emitted
				.subscribe(Util.subscriber());

		producer1
				.startWith(List.of(0,-1,-2))
				.subscribe(Util.subscriber());


	}

	public static void startWith1(){
		var producer1 = Flux.just(1,2,3)
				.doOnSubscribe(s-> log.info("SUBSCRIBING TO PRODUCER-1"))
				.delayElements(Duration.ofMillis(20));

		var producer2 = Flux.just(11,12,13)
				.doOnSubscribe(s-> log.info("SUBSCRIBING TO PRODUCER-2"))
				.delayElements(Duration.ofMillis(20));

		producer1
				.startWith(producer2) //after 1000, will emit the producer2 items before producer1 items
				.startWith(1000) // will emit 1000 as first element
				.subscribe(Util.subscriber());

		Util.sleepThread(2);

		/*
			The closest startWith() to the subscriber, gonna take priority
		 */
	}

	public static void startWith2(){
		var generator = new NameGenerator1();
		generator.generate()
				.take(2)
				.subscribe(Util.subscriber("SUB1"));

		generator.generate()
				.take(3)
				.subscribe(Util.subscriber("SUB2"));

		generator.generate()
				.take(4)
				.subscribe(Util.subscriber("SUB3"));
	}

	public static void concatWithValues(){
		var producer1 = Flux.just(1,2,3)
				.doOnSubscribe(s-> log.info("SUBSCRIBING TO PRODUCER-1"))
				.delayElements(Duration.ofMillis(20));

		var producer2 = Flux.just(11,12,13)
				.doOnSubscribe(s-> log.info("SUBSCRIBING TO PRODUCER-2"))
				.delayElements(Duration.ofMillis(20));

		producer1
				.concatWithValues(4,5) // will emit producer1 values before emitting 4 and 5
				.subscribe(Util.subscriber());

		producer1
				.concatWithValues(4,5) // will emit producer1 values before emitting 4 and 5
				.take(2) // only 1 and 2 will be emitted
				.subscribe(Util.subscriber());

		producer1
				.concatWith(producer2) // will emit producer1 values before emitting producer2 values
				.subscribe(Util.subscriber());

		/*
			The above expression is same as below one
		 */

		Flux.concat(producer1, producer2).subscribe(Util.subscriber());
		Util.sleepThread(2);
	}

	public static void concatWithError(){
		var producer1 = Flux.just(1,2,3)
				.doOnSubscribe(s-> log.info("SUBSCRIBING TO PRODUCER-1"))
				.delayElements(Duration.ofMillis(20));

		var producer2 = Flux.just(11,12,13)
				.doOnSubscribe(s-> log.info("SUBSCRIBING TO PRODUCER-2"))
				.delayElements(Duration.ofMillis(20));
		var producer3 = Flux.error(new RuntimeException("oops"));

		Flux.concat(producer1, producer3, producer2).subscribe(Util.subscriber());
		// will only emit producer1 values, won't go onto the next values of producer2 because of producer3 error
		Flux.concatDelayError(producer1, producer2, producer3).subscribe(Util.subscriber());
		// will emit producer1 values, then producer2 values, then producer3 error
	}

	public static void merge(){
		var producer1 = Flux.just(1,2,3)
				.transform(Util.fluxLogger("producer-1"))
				.delayElements(Duration.ofMillis(20));

		var producer2 = Flux.just(11,12,13)
				.transform(Util.fluxLogger("producer-2"))
				.delayElements(Duration.ofMillis(20));

		Util.sleepThread(2);

		Flux.merge(producer1, producer2).subscribe(Util.subscriber());
		// no specific order of emitted elements, the sub is subscribing to all pubs at the same time
		// the above statement is same as below one

		producer1.mergeWith(producer2).subscribe(Util.subscriber());
	}

	public static void merge1(){
		Flux.merge(Ryanair.ryanair(), Emirates.emirates(), Qatar.qatar())
				.take(Duration.ofSeconds(2))
				.subscribe(Util.subscriber());
		Util.sleepThread(3);
	}

	record Car(String body, String engine, String tires){}
	public static void zip(){
		var bodyFlux = Flux.range(1,5)
				.map(x-> "body: " + x)
				.delayElements(Duration.ofMillis(100));

		var engineFlux = Flux.range(1,3)
				.map(x-> "engine: " + x)
				.delayElements(Duration.ofMillis(200));

		var tiresFlux = Flux.range(1,10)
				.map(x-> "tires: " + x)
				.delayElements(Duration.ofMillis(75));

		Flux.zip(bodyFlux, engineFlux, tiresFlux)
				.map(x-> new Car(x.getT1(), x.getT2(), x.getT3()))
				.subscribe(Util.subscriber());
		Util.sleepThread(5);

		/*
			It will build only 3 objects, since we have only 3 engines
			if any flux was empty, it would've built 0 objects
		 */
	}

	public static void flatMap(){
		/*
			Lat's say we have
			User Service: get User Id for Username, get All users
			Order Service: get Orders by User Id
			I have the username and I want the orders
		 */

		UserService.getUserId("mike")
				.flatMap(PaymentService::getBalance)
				.subscribe(Util.subscriber());
		/*
			Above, flatmap subscribes to Mono pub and emit Mono to a sub
		 */


		UserService.getUserId("mike")
				.flatMapMany(OrderService::getOrders)
				.subscribe(Util.subscriber());
		/*
			Here, flatMapMany subscribes to a Mono and gives a Flux
		 */


		/*
			Get All Orders by Calling getAllUsers
		 */

		UserService.getAllUsers()
				.map(User::id)
				.flatMap(OrderService::getOrders)// It's gonna act like merge
			//	.flatMap(OrderService::getOrders,1)// gonna make ONE concurrent request, it will finish orders of user 1 then 2 then 3
				.subscribe(Util.subscriber());


		Util.sleepThread(5);

		/*
			If you wanna a mapping that completes the 1st flux, then go onto the next one
			see next Method
		 */
	}

	public static void concatMap(){
		UserService.getAllUsers()
				.map(User::id)
				.concatMap(OrderService::getOrders)//equivalent to flatMap(OrderService::getOrders,1)
				.subscribe(Util.subscriber());


		Util.sleepThread(5);
	}

	public static void collectList(){
		var flux = Flux.range(1,10);
		Mono<List<Integer>> monoList = flux.collectList();
	}

	public static void then(){
		saveRecords(List.of("a","b","c"))
				.then() // the pub won't publish the items, it will just emit where whether the operation is complete or not
				.subscribe(Util.subscriber());

		Util.sleepThread(5);
	}

	private static Flux<String> saveRecords(List<String> records){
		return Flux.fromIterable(records)
				.map(r-> "saved: " + r)
				.delayElements(Duration.ofMillis(500));
	}

	public static void then2(){
		var records =List.of("a","b","c");
		saveRecords(records)
				.then(sendNotification(records))
				.subscribe(Util.subscriber());

		Util.sleepThread(5);
	}

	private static Mono<Void> sendNotification(List<String> records){
		return Mono.fromRunnable(()->log.info("ALL RECORDS ARE SAVED SUCCESSFULLY: {}",records));
	}

	public static void buffer(){
		var eventStream = Flux.interval(Duration.ofMillis(200))
				.take(10)
				.map(x-> "EVENT: " + x);
		eventStream
				.buffer() // Collect all as a list
				.subscribe(Util.subscriber());
		Util.sleepThread(60);

		/*
			Running the above, will print the following
			Anonymous RECEIVED ITEM: [EVENT: 0, EVENT: 1, EVENT: 2, EVENT: 3, EVENT: 4, EVENT: 5, EVENT: 6, EVENT: 7, EVENT: 8, EVENT: 9]
		 */
	}

	public static void buffer1(){
		var eventStream = Flux.interval(Duration.ofMillis(200))
				.take(10)
				.map(x-> "EVENT: " + x);
		eventStream
				.buffer(3) // Collect every 3 items
				// each rime, he generates a util.List that contains 3 items
				.subscribe(Util.subscriber());
		Util.sleepThread(60);

		/*
			Running the above, will print the following
			Anonymous RECEIVED ITEM: [EVENT: 0, EVENT: 1, EVENT: 2]
			Anonymous RECEIVED ITEM: [EVENT: 3, EVENT: 4, EVENT: 5]
			Anonymous RECEIVED ITEM: [EVENT: 6, EVENT: 7, EVENT: 8]
			Anonymous RECEIVED ITEM: [EVENT: 9]
			COMPLETED! FOR Anonymous
		 */
	}

	public static void buffer2(){
		var eventStream = Flux.interval(Duration.ofMillis(200))
				.take(10)
				.map(x-> "EVENT: " + x);
		eventStream
				.buffer(Duration.ofMillis(500)) // Collect every 500ms
				.subscribe(Util.subscriber());
		Util.sleepThread(60);

		/*
			Running the above, will print the following
			Anonymous RECEIVED ITEM: [EVENT: 0, EVENT: 1]
			Anonymous RECEIVED ITEM: [EVENT: 2, EVENT: 3]
			Anonymous RECEIVED ITEM: [EVENT: 4, EVENT: 5EVENT: 6]
			Anonymous RECEIVED ITEM: [EVENT: 7, EVENT: 8]
			Anonymous RECEIVED ITEM: [EVENT: 9]
			COMPLETED! FOR Anonymous
		 */
	}

	public static void buffer3(){
		var eventStream = Flux.interval(Duration.ofMillis(200))
				.take(10)
				.concatWith(Flux.never()) // doesn't emit anything
				.map(x-> "EVENT: " + x);
		eventStream
				.buffer(3) // Collect every 3
				.subscribe(Util.subscriber());
		Util.sleepThread(60);

		/*
			Running the above, will print the following
			Anonymous RECEIVED ITEM: [EVENT: 0, EVENT: 1, EVENT: 2]
			Anonymous RECEIVED ITEM: [EVENT: 3, EVENT: 4, EVENT: 5]
			Anonymous RECEIVED ITEM: [EVENT: 6, EVENT: 7, EVENT: 8]

			it will never complete, it keeps waiting for the 9th Event
			The solution is in the nest method
		 */
	}

	public static void buffer4(){
		var eventStream = Flux.interval(Duration.ofMillis(200))
				.take(10)
				.concatWith(Flux.never()) // doesn't emit anything
				.map(x-> "EVENT: " + x);
		eventStream
				.bufferTimeout(3,Duration.ofSeconds(1)) // Collect every 3 and don't wait for more than one second
				.subscribe(Util.subscriber());
		Util.sleepThread(60);

		/*
			Running the above, will print the following
			Anonymous RECEIVED ITEM: [EVENT: 0, EVENT: 1, EVENT: 2]
			Anonymous RECEIVED ITEM: [EVENT: 3, EVENT: 4, EVENT: 5]
			Anonymous RECEIVED ITEM: [EVENT: 6, EVENT: 7, EVENT: 8]
			Anonymous RECEIVED ITEM: [EVENT: 9]
			COMPLETED! FOR Anonymous
		 */
	}

	public static void window(){
		Flux.interval(Duration.ofMillis(200))
				.map(x-> "EVENT-" + x)
				.window(5) // It will open a flux for every 5 item
			//	.window(Duration.ofMillis(2000)) // it open a flux for every 2 secs
				.flatMap(ReactiveSec1Application::processFlux)
				.subscribe();

		Util.sleepThread(60);

		/*
			Running the above will print the following:
			*****
			*****
			*****
			*****
			*****
			.
			.
			.
			*****
		 */
	}

	private static Mono<Void> processFlux(Flux<String> innerFlux){
		return innerFlux.doOnNext(e-> System.out.print("*"))
				.doOnComplete(System.out::println)
				.then();
	}

	public static void groupBy(){
		Flux.range(1,30)
				.delayElements(Duration.ofMillis(1000))
				.groupBy(i->i%2) //Group by number parity, either 0 or 1
				.flatMap(x -> processEvents(x))
				.subscribe();
		Util.sleepThread(60);
	}

	private static Mono<Void> processEvents(GroupedFlux<Integer,Integer> flux){
		log.info("RECEIVED GROUPED FLUX FOR KEY: {}", flux.key());
		return flux.doOnNext(item -> log.info("RECEIVED ITEM: {} FOR KEY {}",item,flux.key()))
				.then();
	}

	public static void repeat(){
		var mono = Mono.fromSupplier(() -> Util.faker().country().name());
		var sub = Util.subscriber();
		mono
				.repeat() // Now the Mono is a FLUX!
				// It will resubscribe again and again infinitely ONLY AFTER IT RECEIVES THE COMPLETE SIGNAL
			//	.repeat(3) // It will resubscribe 3 more times, now the sub gonna get 4 items
				.subscribe(sub);
	}

	public static void repeat1(){
		var mono = Mono.fromSupplier(() -> Util.faker().country().name());
		var sub = Util.subscriber();
		mono
				.repeat()
				.takeUntil(x-> x.equalsIgnoreCase("canada"))
				.subscribe(sub);
	}

	public static void repeat2(){
		var atomicInteger = new AtomicInteger(0);
		var mono = Mono.fromSupplier(() -> Util.faker().country().name());
		var sub = Util.subscriber();
		mono
				.repeat(()-> atomicInteger.incrementAndGet()<3) // It will repeat until the number becomes equal to 3
				.subscribe(sub);
	}

	public static void repeat3(){
		var mono = Mono.fromSupplier(() -> Util.faker().country().name());
		var sub = Util.subscriber();
		mono
				.repeatWhen(flux->flux.delayElements(Duration.ofSeconds(2))) // repeat after 2 secs
				.subscribe(sub);
	}

	public static void repeat4(){
		var mono = Mono.fromSupplier(() -> Util.faker().country().name());
		var sub = Util.subscriber();
		mono
				.repeatWhen(flux->flux.delayElements(Duration.ofSeconds(2)).take(2)) // repeat at max 2 times, each one after 2 secs
				.subscribe(sub);
	}

	public static void retry(){
		var atomicInteger = new AtomicInteger(0);
		var mono = Mono.fromSupplier(() -> {
			if(atomicInteger.incrementAndGet()<3){
				throw new RuntimeException("retry fail");
			}
			return Util.faker().country().name();
		});
		var sub = Util.subscriber();
		mono
			//	.retry(1) it will retry 1 more time, taht's not enough! it has to retry 2 times to return country name
				.retry(2)
				.subscribe(sub);
	}

	public static void retry1(){
		var atomicInteger = new AtomicInteger(0);
		var mono = Mono.fromSupplier(() -> {
			if(atomicInteger.incrementAndGet()<3){
				throw new RuntimeException("retry fail");
			}
			return Util.faker().country().name();
		});
		var sub = Util.subscriber();
		mono
				.retryWhen(Retry.fixedDelay(
						2,Duration.ofSeconds(1)) // retry 2 times, 2 secs between each
						.filter(ex-> RuntimeException.class.equals(ex.getClass())) // ONLY Retry when the error is runtime exception
						.onRetryExhaustedThrow((spec,signal) -> signal.failure()) // Show the original exception instead of retryExhausted exception
						.doBeforeRetry(rs-> log.info("RETRYING")))
				.subscribe(sub);
	}

	public static void sinkOne_TryEmitValue(){
		/*
			I can emit values without having subs with sinks
		 */
		var sink = Sinks.one();
		sink.tryEmitValue("Hi");
		var mono = sink.asMono();
		mono.subscribe(Util.subscriber());
	}

	public static void sinkOne_TryEmitEmpty(){
		var sink = Sinks.one();
		sink.tryEmitEmpty();
		var mono = sink.asMono();
		mono.subscribe(Util.subscriber());
	}

	public static void sinkOne_TryEmitError(){
		var sink = Sinks.one();
		sink.tryEmitError(new RuntimeException("oops"));
		var mono = sink.asMono();
		mono.subscribe(Util.subscriber());
	}

	public static void sinkOne_multipleSubscribers(){
		var sink = Sinks.one();
		sink.tryEmitValue("Hi");
		var mono = sink.asMono();
		mono.subscribe(Util.subscriber("sub1"));
		mono.subscribe(Util.subscriber("sub2"));
	}

	public static void sinkOne_FailureHandler(){
		var sink = Sinks.one();
		sink.tryEmitValue("Hi"); //It will merely try to emit values, in case of issues, it won't notify us
		var mono = sink.asMono();
		mono.subscribe(Util.subscriber("sub1"));

		/*
			since sink type is One().
			And since sinks already emitted one value, it will face issues
			when requested to emit another one like it will next statements.

		 */
		sink.emitValue("HELLO",((signalType, emitResult) -> {
			log.info(signalType.toString());
			log.info(emitResult.toString());
			return false; // here the sink won't retry, if it was set to true, it will. but would never work as it already emitted a value
		}));
	}

	public static void sinkMany_unicast(){
		// handle through which we would push items
		// onBackPressureBuffer - unbounded queue
		var sink = Sinks.many().unicast().onBackpressureBuffer();
		var flux = sink.asFlux();
		sink.tryEmitNext("Hi");
		sink.tryEmitNext("HELLO");
		sink.tryEmitNext("HELLO");
		flux.subscribe(Util.subscriber());

		/*
			With sink unicast, we can emit many values,
			we can have ONE sub at most!
		 */
	}

	public static void sink_ThreadSafety(){
		var sink = Sinks.many().unicast().onBackpressureBuffer();
		var flux = sink.asFlux();
		var list = new ArrayList<>();
		flux.subscribe(list::add);
		for(int i=0;i<1000;i++){
			var j = i;
			CompletableFuture.runAsync(()->{
				sink.tryEmitNext(j);
			});
		}
		Util.sleepThread(10);
		log.info("LIST SIZE: {}", list.size()); // it won't print 999
	}
	public static void sink_ThreadSafety1(){
		var sink = Sinks.many().unicast().onBackpressureBuffer();
		var flux = sink.asFlux();
		var list = new ArrayList<>();
		flux.subscribe(list::add);
		for(int i=0;i<1000;i++){
			var j = i;
			CompletableFuture.runAsync(()->{
				sink.emitNext(j,((signalType, emitResult) -> {
					return Sinks.EmitResult.FAIL_NON_SERIALIZED.equals(emitResult);
				}));
			});
		}
		Util.sleepThread(10);
			log.info("LIST SIZE: {}", list.size()); // it WILL print 999
	}

	public static void sinkMany_multicast(){
		// handle through which we would push items
		// onBackPressureBuffer - bounded queue
		var sink = Sinks.many().multicast().onBackpressureBuffer();
		var flux = sink.asFlux();
		flux.subscribe(Util.subscriber("sub1"));
		flux.subscribe(Util.subscriber("sub2"));

		sink.tryEmitNext("Hi");
		sink.tryEmitNext("HELLO");
		sink.tryEmitNext("HELLO");

		Util.sleepThread(10);

		flux.subscribe(Util.subscriber("sub3"));

		sink.tryEmitNext("new Message");
		/*
			With sink multicast, we can emit many values,
			we can have more subs.
			if a sub joins late, won't be able to see the missed message.
			Here both sub1 and sub2 will receive all the messages
			Sub3 will only receive the 'new message', won't be able
			to see past messages
		 */
	}

	public static void sinkMany_multicast2(){
		// handle through which we would push items
		// onBackPressureBuffer - bounded queue
		var sink = Sinks.many().multicast().onBackpressureBuffer();
		var flux = sink.asFlux();

		sink.tryEmitNext("Hi");
		sink.tryEmitNext("HELLO");
		sink.tryEmitNext("HELLO");
		Util.sleepThread(10);

		flux.subscribe(Util.subscriber("sub1"));
		flux.subscribe(Util.subscriber("sub2"));
		flux.subscribe(Util.subscriber("sub3"));
		sink.tryEmitNext("new Message");
		/*
			Here only sub1 who's gonna receive all the messages
			sub2 and sub3 will only receive 'new message'
		 */
	}

	private static void sinkMany_multicast3() {

		System.setProperty("reactor.bufferSize.small", "16");

		// handle through which we would push items
		// onBackPressureBuffer - bounded queue
		var sink = Sinks.many().multicast().onBackpressureBuffer();//sam will recieve SOME of the mssages, not ALL

		// handle through which subscribers will receive items
		var flux = sink.asFlux();

		flux.subscribe(Util.subscriber("sam"));
		flux.delayElements(Duration.ofMillis(200)).subscribe(Util.subscriber("mike"));

		/*
			Here sam won't receive ALL the messages, because the other sub is slow
			With multicast().onBackpressureBuffer(), the performance of slow sub affect the performance
			of the fast sub
		 */

		for (int i = 1; i <= 100; i++) {
			var result = sink.tryEmitNext(i);
			log.info("item: {}, result: {}", i, result);
		}
	}

		private static void sinkMany_multicast4() {

			System.setProperty("reactor.bufferSize.small", "16");

			// handle through which we would push items
			// onBackPressureBuffer - bounded queue
			var sink = Sinks.many().multicast().directBestEffort(); // sam will receive all the messages

			// handle through which subscribers will receive items
			var flux = sink.asFlux();

			flux.subscribe(Util.subscriber("sam"));
			flux.delayElements(Duration.ofMillis(200)).subscribe(Util.subscriber("mike"));

		/*
			Now Sam will receive ALL the messages, while mike is gonna be ignored
			the sink here will focus on the fast sub only
			If you want mike to the receive the messages as well, see nextMethod
		 */

			for (int i = 1; i <= 100; i++) {
				var result = sink.tryEmitNext(i);
				log.info("item: {}, result: {}", i, result);
			}
		}

	private static void sinkMany_multicast5() {

		System.setProperty("reactor.bufferSize.small", "16");

		// handle through which we would push items
		// onBackPressureBuffer - bounded queue
		var sink = Sinks.many().multicast().directBestEffort();

		// handle through which subscribers will receive items
		var flux = sink.asFlux();

		flux.subscribe(Util.subscriber("sam"));
		flux
				.onBackpressureBuffer() // telling the sink, "I am a slow sub"
				.delayElements(Duration.ofMillis(200)).subscribe(Util.subscriber("mike"));

		/*
			Now both sam and mike will receive the messages
		 */

		for (int i = 1; i <= 100; i++) {
			var result = sink.tryEmitNext(i);
			log.info("item: {}, result: {}", i, result);
		}
	}

	private static void sinkMany_multicast6() {

		System.setProperty("reactor.bufferSize.small", "16");

		// handle through which we would push items
		// onBackPressureBuffer - bounded queue
		var sink = Sinks.many().multicast().directAllOrNothing(); // if a sub is slow, don't deliver to anyone!

		// handle through which subscribers will receive items
		var flux = sink.asFlux();

		flux.subscribe(Util.subscriber("sam"));
		flux.delayElements(Duration.ofMillis(200)).subscribe(Util.subscriber("mike"));

		/*
			Here both sam and mike won't receive any messages, since one of them is slow
		 */

		for (int i = 1; i <= 100; i++) {
			var result = sink.tryEmitNext(i);
			log.info("item: {}, result: {}", i, result);
		}
	}

	public static void sinkMany_replay(){
		// handle through which we would push items
		// onBackPressureBuffer - bounded queue
		var sink = Sinks.many().replay().all();
		var flux = sink.asFlux();
		flux.subscribe(Util.subscriber("sub1"));
		flux.subscribe(Util.subscriber("sub2"));

		sink.tryEmitNext("Hi");
		sink.tryEmitNext("HELLO");
		sink.tryEmitNext("HELLO");

		Util.sleepThread(10);

		flux.subscribe(Util.subscriber("sub3"));

		sink.tryEmitNext("new Message");
		/*
			With sink replay, we can emit many values,
			we can have more subs.
			ALL subs will receive ALL the messages, be it early subs or past ones
			Sometimes you requirement requires the late sub should be able to see
			only the last 2 messages or messages of the last 2 minutes
			if so, see next method
		 */
	}

	public static void sinkMany_replay2(){
		// handle through which we would push items
		// onBackPressureBuffer - bounded queue
		var sink = Sinks.many().replay().limit(1);
		var flux = sink.asFlux();
		flux.subscribe(Util.subscriber("sub1"));
		flux.subscribe(Util.subscriber("sub2"));

		sink.tryEmitNext("msg1");
		sink.tryEmitNext("msg2");
		sink.tryEmitNext("msg3");

		Util.sleepThread(10);

		flux.subscribe(Util.subscriber("sub3"));

		sink.tryEmitNext("msg4");
		/*
			Now sub3 only gonna see 'msg3' and 'msg4'
		 */
	}





}
