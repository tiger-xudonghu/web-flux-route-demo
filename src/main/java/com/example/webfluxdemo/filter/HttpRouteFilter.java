package com.example.webfluxdemo.filter;


import com.example.webfluxdemo.client.NettyHttpClient;
import com.example.webfluxdemo.core.Constants;
import com.example.webfluxdemo.core.GatewayFilterChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class HttpRouteFilter extends BaseGatewayFilter {
    private final NettyHttpClient nettyHttpClient;
    public HttpRouteFilter(NettyHttpClient nettyHttpClient) {
        this.nettyHttpClient = nettyHttpClient;
    }

    @Override
    public Mono<Void> doExecute(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        log.info("into HttpRouteFilter ...");
        String realURL = buildRealURL(exchange);
        exchange.getAttributes().put(Constants.HTTP_URL, realURL);
        //设置下超时时间
        exchange.getAttributes().put(Constants.HTTP_TIME_OUT, 10000L);
        return nettyHttpClient.execute(exchange,chain).then(chain.execute(exchange));
    }

    @Override
    public int getOrder() {
        return 100;
    }



    private String buildRealURL(final ServerWebExchange exchange) {
        String url = "http://192.168.5.17"+exchange.getRequest().getPath().toString();
        String query = exchange.getRequest().getURI().getQuery();
        if (Strings.isNotBlank(query)) {
            return url + "?" + query;
        }
        return url;
    }
}
