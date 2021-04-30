package com.example.webfluxdemo.client;

import com.example.webfluxdemo.core.Constants;
import com.example.webfluxdemo.core.GatewayFilterChain;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeoutException;


@Slf4j
public class NettyHttpClient {
    private final HttpClient httpClient;

    public NettyHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public  Flux<HttpClientResponse> execute(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders filtered = request.getHeaders();
        final HttpMethod method = HttpMethod.valueOf(request.getMethodValue());
        final DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        filtered.forEach(httpHeaders::set);
        String url = exchange.getAttribute(Constants.HTTP_URL);

 Flux<HttpClientResponse> responseFlux = this.httpClient
                .headers(headers -> headers.add(httpHeaders))
                .request(method).uri(url)
                .send((req, nettyOutbound) ->{
                            if (log.isTraceEnabled()) {
                                nettyOutbound.withConnection(connection -> log.trace("outbound route: "
                                        + connection.channel().id().asShortText() + ", inbound: " + exchange.getLogPrefix()));
                            }
                            return nettyOutbound.send(request.getBody().map(this::getByteBuf));
                        })
                .responseConnection((res, connection) -> {
                    // Defer committing the response until all route filters have run
                    // Put client response as ServerWebExchange attribute and write
                    // response later NettyWriteResponseFilter

                    exchange.getAttributes().put(Constants.CLIENT_RESPONSE_ATTR, res);
                    exchange.getAttributes().put(Constants.CLIENT_RESPONSE_CONN_ATTR, connection);

                    ServerHttpResponse response = exchange.getResponse();
                    HttpHeaders headers = new HttpHeaders();

                    res.responseHeaders().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));

                    String contentTypeValue = headers.getFirst(HttpHeaders.CONTENT_TYPE);
                    if (StringUtils.hasLength(contentTypeValue)) {
                        exchange.getAttributes().put(Constants.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR, contentTypeValue);
                    }
                    setResponseStatus(res, response);

                    response.getHeaders().putAll(headers);

                    return Mono.just(res);
                });

        long timeout = (long) Optional.ofNullable(exchange.getAttribute(Constants.HTTP_TIME_OUT)).orElse(5000L);
        Duration duration = Duration.ofMillis(timeout);

        return responseFlux
                .timeout(duration,
                Mono.error(new TimeoutException("Response took longer than timeout: " + duration)))
                .onErrorMap(TimeoutException.class,
                        th -> new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, th.getMessage(), th));

    }



    private void setResponseStatus(HttpClientResponse clientResponse, ServerHttpResponse response) {
        HttpStatus status = HttpStatus.resolve(clientResponse.status().code());
        if (status != null) {
            response.setStatusCode(status);
        }
        else {
            while (response instanceof ServerHttpResponseDecorator) {
                response = ((ServerHttpResponseDecorator) response).getDelegate();
            }
            if (response instanceof AbstractServerHttpResponse) {
                ((AbstractServerHttpResponse) response).setStatusCodeValue(clientResponse.status().code());
            }
            else {
                // TODO: log warning here, not throw error?
                throw new IllegalStateException("Unable to set status code " + clientResponse.status().code()
                        + " on response of type " + response.getClass().getName());
            }
        }
    }


    protected ByteBuf getByteBuf(DataBuffer dataBuffer) {
        if (dataBuffer instanceof NettyDataBuffer) {
            NettyDataBuffer buffer = (NettyDataBuffer) dataBuffer;
            return buffer.getNativeBuffer();
        }
        // MockServerHttpResponse creates these
        else if (dataBuffer instanceof DefaultDataBuffer) {
            DefaultDataBuffer buffer = (DefaultDataBuffer) dataBuffer;
            return  Unpooled.wrappedBuffer(buffer.getNativeBuffer());
        }
        throw new IllegalArgumentException("Unable to handle DataBuffer of type " + dataBuffer.getClass());
    }

    private String convertByteBufToString(ByteBuf buf) {
        String str;
        if(buf.hasArray()) { // 处理堆缓冲区
            str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else { // 处理直接缓冲区以及复合缓冲区
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            str = new String(bytes, 0, buf.readableBytes());
        }
        return str;
    }


}
