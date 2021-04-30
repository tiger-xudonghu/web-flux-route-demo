package com.example.webfluxdemo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SZ
 * @Description:
 * @date 2021/4/29 14:34
 */
public class DefaultGatewayFilterChain  implements GatewayFilterChain {

    private static final Logger log = LoggerFactory.getLogger(DefaultGatewayFilterChain.class);
    private int index;
    private List<GatewayFilter> filters = new ArrayList();

    public DefaultGatewayFilterChain() {
        GatewayFilterRegister.getInstance().sorted();
        this.filters.addAll(GatewayFilterRegister.getInstance().getGatewayFilters());
        this.index = 0;
    }

    public List<GatewayFilter> getFilters() {
        return this.filters;
    }

    public void refresh() {
        List<GatewayFilter> gatewayFilters = GatewayFilterRegister.getInstance().getGatewayFilters();
        if (this.index == 0 && this.filters.size() != gatewayFilters.size()) {
            this.filters.clear();
            this.filters.addAll(gatewayFilters);
        }

    }

    @Override
    public Mono<Void> execute(ServerWebExchange exchange) {
        this.refresh();
        return Mono.defer(() -> {
            if (this.index < this.filters.size()) {
                GatewayFilter gatewayFilter = (GatewayFilter)this.filters.get(this.index++);
                Boolean skip = gatewayFilter.skip(exchange);
                if (skip) {
                    log.info("requestId {}, skip filter ====> {}", exchange.getAttribute("requestId"), gatewayFilter.getClass().getSimpleName());
                    return this.execute(exchange);
                } else {
                    return gatewayFilter.execute(exchange, this);
                }
            } else {
                return Mono.empty();
            }
        });
    }
}
