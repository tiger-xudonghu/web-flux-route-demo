package com.example.webfluxdemo;

import com.example.webfluxdemo.client.NettyHttpClient;
import com.example.webfluxdemo.core.GatewayFilterRegister;
import com.example.webfluxdemo.filter.HttpResponseFilter;
import com.example.webfluxdemo.filter.HttpRouteFilter;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WebFluxDemoApplication {

    @SneakyThrows
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(WebFluxDemoApplication.class, args);


        NettyHttpClient nettyHttpClient = applicationContext.getBean(NettyHttpClient.class);

        HttpRouteFilter httpRouteFilter = new HttpRouteFilter(nettyHttpClient);


        HttpResponseFilter responseFilter = new HttpResponseFilter();

        GatewayFilterRegister.getInstance().register(httpRouteFilter);
        GatewayFilterRegister.getInstance().register(responseFilter);


    }




}
