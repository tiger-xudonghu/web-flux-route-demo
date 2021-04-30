package com.example.webfluxdemo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * @author SZ
 * @Description:
 * @date 2021/4/29 14:31
 */
public class GatewayWebHandler implements WebHandler {
    private static final Logger log = LoggerFactory.getLogger(GatewayWebHandler.class);
    private Scheduler scheduler = Schedulers.elastic();
    private final ApplicationContext context;

    public GatewayWebHandler(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange) {
        return (new DefaultGatewayFilterChain()).execute(serverWebExchange).subscribeOn(this.scheduler).doOnSuccess((t) -> {
            System.out.println("GatewayWebHandler -> handler " );
        }).doOnError((t) -> {
            System.err.println("GatewayWebHandler -> handler " );
        });
    }
}
