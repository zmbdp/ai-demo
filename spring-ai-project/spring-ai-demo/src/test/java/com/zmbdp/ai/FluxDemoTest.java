package com.zmbdp.ai;

import reactor.core.publisher.Flux;

import java.time.Duration;

public class FluxDemoTest {

    public static void main(String[] args) throws InterruptedException {
        Flux<String> flux = Flux.just("a", "b", "c", "d", "e", "f").delayElements(Duration.ofSeconds(1));
        flux.map(String::toUpperCase).subscribe(System.out::println);
        Thread.sleep(10000);
    }
}
