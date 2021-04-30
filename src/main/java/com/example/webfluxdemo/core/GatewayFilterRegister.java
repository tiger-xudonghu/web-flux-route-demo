package com.example.webfluxdemo.core;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GatewayFilterRegister {


    private List<GatewayFilter> gatewayFilters = new ArrayList<>();

    private static class GatewayFilterManagerHolder{
        private static final GatewayFilterRegister INSTANCE = new GatewayFilterRegister();
    }

    private GatewayFilterRegister(){}

    public static GatewayFilterRegister getInstance(){
        return GatewayFilterManagerHolder.INSTANCE;
    }

    public void register(GatewayFilter filter){
        gatewayFilters.add(filter);
    }

    public List<GatewayFilter> getGatewayFilters() {
        return gatewayFilters;
    }

    public GatewayFilter get(int index){
        return gatewayFilters.get(index);
    }

    /**
     * 排序
     */
    public void sorted(){
        gatewayFilters = gatewayFilters.stream()
                .sorted(Comparator.comparingInt(GatewayFilter::getOrder)).collect(Collectors.toList());

    }

}
