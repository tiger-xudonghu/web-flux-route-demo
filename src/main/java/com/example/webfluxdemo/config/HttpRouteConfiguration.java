package com.example.webfluxdemo.config;

import com.example.webfluxdemo.client.NettyHttpClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.ProxyProvider;

import java.security.cert.X509Certificate;
import java.time.Duration;

/**
 * @author SZ
 * @Description:
 * @date 2021/4/29 14:46
 */
@Configuration
public class HttpRouteConfiguration {

    @Bean
    public HttpClientProperties httpClientProperties() {
        return new HttpClientProperties();
    }

    @Bean
    public NettyHttpClient nettyHttpClient(final ObjectProvider<HttpClient> httpClient){
        return new NettyHttpClient(httpClient.getIfAvailable());
    }

    @Bean
    public HttpClient httpClient(final HttpClientProperties properties) {
        // configure pool resources
        HttpClientProperties.Pool pool = properties.getPool();

        ConnectionProvider connectionProvider = ConnectionProvider.builder(pool.getName())
                .maxConnections(Integer.MAX_VALUE)
                .pendingAcquireTimeout(Duration.ofMillis(0))
                .pendingAcquireMaxCount(-1)
                .maxIdleTime(Duration.ofMillis(10000))
                .maxLifeTime(Duration.ofMillis(1000))
                .build();



        HttpClient httpClient = HttpClient.create(connectionProvider)
                .tcpConfiguration(tcpClient -> {
                    if (properties.getConnectTimeout() != null) {
                        tcpClient = tcpClient.option(
                                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                properties.getConnectTimeout());
                    }
                    // configure proxy if proxy host is set.
                    HttpClientProperties.Proxy proxy = properties.getProxy();
                    if (StringUtils.hasText(proxy.getHost())) {
                        tcpClient = tcpClient.proxy(proxySpec -> {
                            ProxyProvider.Builder builder = proxySpec
                                    .type(ProxyProvider.Proxy.HTTP)
                                    .host(proxy.getHost());
                            PropertyMapper map = PropertyMapper.get();
                            map.from(proxy::getPort).whenNonNull().to(builder::port);
                            map.from(proxy::getUsername).whenHasText()
                                    .to(builder::username);
                            map.from(proxy::getPassword).whenHasText()
                                    .to(password -> builder.password(s -> password));
                            map.from(proxy::getNonProxyHostsPattern).whenHasText()
                                    .to(builder::nonProxyHosts);
                        });
                    }
                    tcpClient = tcpClient.option(ChannelOption.SO_TIMEOUT, 5000);
//                    return tcpClient.doOnDisconnected(DisposableChannel::dispose);
                    return tcpClient;
                });

        HttpClientProperties.Ssl ssl = properties.getSsl();

        ssl.setUseInsecureTrustManager(true);
        if (ssl.getTrustedX509CertificatesForTrustManager().length > 0
                || ssl.isUseInsecureTrustManager()) {
            httpClient = httpClient.secure(sslContextSpec -> {
                // configure ssl
                SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
                X509Certificate[] trustedX509Certificates = ssl
                        .getTrustedX509CertificatesForTrustManager();
                if (trustedX509Certificates.length > 0) {
                    sslContextBuilder.trustManager(trustedX509Certificates);
                } else if (ssl.isUseInsecureTrustManager()) {
                    sslContextBuilder
                            .trustManager(InsecureTrustManagerFactory.INSTANCE);
                }
                sslContextSpec.sslContext(sslContextBuilder)
                        .defaultConfiguration(ssl.getDefaultConfigurationType())
                        .handshakeTimeout(ssl.getHandshakeTimeout())
                        .closeNotifyFlushTimeout(ssl.getCloseNotifyFlushTimeout())
                        .closeNotifyReadTimeout(ssl.getCloseNotifyReadTimeout());
            });
        }

        if (properties.isWiretap()) {
            httpClient = httpClient.wiretap(true);
        }

        return httpClient;
    }

}
