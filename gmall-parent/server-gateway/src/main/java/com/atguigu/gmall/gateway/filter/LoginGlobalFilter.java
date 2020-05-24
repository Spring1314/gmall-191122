package com.atguigu.gmall.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;

/**
 * @author Administrator
 * @create 2020-05-24 14:28
 */
@Component
//@Order(0)
public class LoginGlobalFilter implements GlobalFilter,Order{

    //过滤
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //请求
        ServerHttpRequest request = exchange.getRequest();
        //响应
        ServerHttpResponse response = exchange.getResponse();
        //路径 uri
        String path = request.getURI().getPath();
        //网址 url
        String rawSchemeSpecificPart = request.getURI().getRawSchemeSpecificPart();
        //放行
        return chain.filter(exchange);
    }

    //设置过滤器执行的优先级
    @Override
    public int value() {
        return 0;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
