package com.example.webfluxdemo.filter;

import com.example.webfluxdemo.core.GatewayFilterChain;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

import java.util.Arrays;
import java.util.List;

import static com.example.webfluxdemo.core.Constants.CLIENT_RESPONSE_CONN_ATTR;


@Slf4j
public class HttpResponseFilter extends BaseGatewayFilter {
    private final List<MediaType> streamingMediaTypes = Arrays.asList(MediaType.TEXT_EVENT_STREAM, MediaType.APPLICATION_STREAM_JSON);

    public HttpResponseFilter() {
    }

    @Override
    public Mono<Void> doExecute(ServerWebExchange exchange, GatewayFilterChain chain) {
        return Mono.defer(() -> {
                    Connection connection = exchange.getAttribute(CLIENT_RESPONSE_CONN_ATTR);

                    if (connection == null) {
                        return Mono.empty();
                    }
                    if (log.isTraceEnabled()) {
                        log.trace("NettyWriteResponseFilter start inbound: "
                                + connection.channel().id().asShortText() + ", outbound: "
                                + exchange.getLogPrefix());
                    }
                    ServerHttpResponse response = exchange.getResponse();

                    // TODO: needed?
                    final Flux<DataBuffer> body = connection
                            .inbound()
                            .receive()
                            .retain()
                            .map(byteBuf -> wrap(byteBuf, response));

                    MediaType contentType = null;
                    try {
                        contentType = response.getHeaders().getContentType();
                    }
                    catch (Exception e) {
                        if (log.isTraceEnabled()) {
                            log.trace("invalid media type", e);
                        }
                    }
                    return (isStreamingMediaType(contentType)
                            ? response.writeAndFlushWith(body.map(Flux::just))
                            : response.writeWith(body));
                }).doOnError(throwable -> cleanup(exchange)).doOnCancel(() -> cleanup(exchange));
        // @formatter:on

    }

    @Override
    public int getOrder() {
        return 200;
    }

    protected DataBuffer wrap(ByteBuf byteBuf, ServerHttpResponse response) {
        DataBufferFactory bufferFactory = response.bufferFactory();
        if (bufferFactory instanceof NettyDataBufferFactory) {
            NettyDataBufferFactory factory = (NettyDataBufferFactory) bufferFactory;
            return factory.wrap(byteBuf);
        }
        // MockServerHttpResponse creates these
        else if (bufferFactory instanceof DefaultDataBufferFactory) {
            DataBuffer buffer = ((DefaultDataBufferFactory) bufferFactory).allocateBuffer(byteBuf.readableBytes());
            buffer.write(byteBuf.nioBuffer());
            byteBuf.release();
            return buffer;
        }
        throw new IllegalArgumentException("Unkown DataBufferFactory type " + bufferFactory.getClass());
    }


    private void cleanup(final ServerWebExchange exchange) {
        Connection connection = exchange.getAttribute(CLIENT_RESPONSE_CONN_ATTR);
        if (connection != null) {
            connection.dispose();
        }
    }

    private boolean isStreamingMediaType(@Nullable final MediaType contentType) {
        return contentType != null && this.streamingMediaTypes.stream().anyMatch(contentType::isCompatibleWith);
    }

}
