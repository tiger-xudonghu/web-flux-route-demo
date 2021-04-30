package com.example.webfluxdemo.core;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author SZ
 * @Description:
 * @date 2021/4/29 14:31
 */
public interface GatewayFilterChain {
    Mono<Void> execute(ServerWebExchange exchange);
}
