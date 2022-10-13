package com.rsw.moviesinfoservice.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    void sink() {
        //given
        Sinks.Many<Integer> replaySinks = Sinks.many().replay().all();

        //when
        replaySinks.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySinks.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        Flux<Integer> integerFlux = replaySinks.asFlux();
        integerFlux.subscribe((i) -> {
            System.out.println("Subscriber 1: " + i);
        });

        Flux<Integer> integerFlux1 = replaySinks.asFlux();
        integerFlux1.subscribe((i) -> {
            System.out.println("Subscriber 2: " + i);
        });

        replaySinks.tryEmitNext(3);

        Flux<Integer> integerFlux2 = replaySinks.asFlux();
        integerFlux2.subscribe((i) -> {
            System.out.println("Subscriber 3: " + i);
        });
    }

    @Test
    void sink_multicast() {
        // given
        Sinks.Many<Integer> multiCast = Sinks.many().multicast().onBackpressureBuffer();

        //when
        multiCast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multiCast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = multiCast.asFlux();
        integerFlux.subscribe((i) -> {
            System.out.println("Subscriber 1: " + i);
        });

        Flux<Integer> integerFlux1 = multiCast.asFlux();
        integerFlux1.subscribe((i) -> {
            System.out.println("Subscriber 2: " + i);
        });

        multiCast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Test
    void sink_unicast() {
        // given
        Sinks.Many<Integer> multiCast = Sinks.many().unicast().onBackpressureBuffer();

        //when
        multiCast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multiCast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = multiCast.asFlux();
        integerFlux.subscribe((i) -> {
            System.out.println("Subscriber 1: " + i);
        });

        Flux<Integer> integerFlux1 = multiCast.asFlux();
        integerFlux1.subscribe((i) -> {
            System.out.println("Subscriber 2: " + i);
        });

        multiCast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
