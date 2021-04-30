package com.example.webfluxdemo.core;

import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface GatewayFilter extends Ordered {

    Mono<Void> execute(ServerWebExchange exchange, GatewayFilterChain chain);

    /**
     * 如果返回为true时，跳过此filter
     * @param exchange  the current server exchange
     * @return default false.
     */
    default Boolean skip(ServerWebExchange exchange) {
        return false;
    }

}
