package com.example.webfluxdemo.filter;

import com.example.webfluxdemo.core.GatewayFilter;
import com.example.webfluxdemo.core.GatewayFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * @author SZ
 * @Description:
 * @date 2021/4/29 14:40
 */

public abstract class BaseGatewayFilter implements GatewayFilter {
    private static final Logger log = LoggerFactory.getLogger(BaseGatewayFilter.class);


    public BaseGatewayFilter( ) {
    }

    public abstract Mono<Void> doExecute(ServerWebExchange var1, GatewayFilterChain var2);

    @Override
    public Mono<Void> execute(ServerWebExchange exchange, GatewayFilterChain chain) {
        return this.doExecute(exchange, chain);

    }


    @Override
    public Boolean skip(ServerWebExchange exchange) {
        return false;
    }
}

