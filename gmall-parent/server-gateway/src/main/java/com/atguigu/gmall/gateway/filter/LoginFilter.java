package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;

/**
 * @author Administrator
 * @create 2020-05-25 21:11
 */
@Component
public class LoginFilter implements GlobalFilter, Order {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${auth.url}")
    private String[] authUrl;

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    //过滤
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        //1.浏览器无权访问/inner内部资源
        if (antPathMatcher.match("/inner/**",path)){
            out(response,ResultCodeEnum.PERMISSION);
        }
        //获得用户id
        String userId = getUserId(request);
        //2.访问/auth,需要登录状态
        if(antPathMatcher.match("/auth/**",path) && StringUtils.isEmpty(userId)){
            out(response,ResultCodeEnum.LOGIN_AUTH);
        }

        //3.判断是否刷新页面
        for (String url : authUrl) {
            if (path.contains(url)){
                //重定向
                response.setStatusCode(HttpStatus.SEE_OTHER);
                String rawSchemeSpecificPart = request.getURI().getRawSchemeSpecificPart();
                try {
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://passport.gmall.com/login.html?originUrl="+
                            URLEncoder.encode(rawSchemeSpecificPart,"utf-8"));
                    //响应给浏览器
                    response.setComplete();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        //获得临时用户id
        String userTempId = getUserTempId(request);
        if (!StringUtils.isEmpty(userTempId)){
            request.mutate().header("userTempId",userTempId);
        }

        //将userId传给后续页面
        if (!StringUtils.isEmpty(userId))
        request.mutate().header("userId",userId);
        return chain.filter(exchange);
    }
    //获得临时用户id
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = request.getHeaders().getFirst("userTempId");
        if (userTempId==null){
            HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
            if (httpCookie != null){
                userTempId = httpCookie.getValue();
            }
        }
        return userTempId;
    }

    //获得用户id
    private String getUserId(ServerHttpRequest request) {
        //header cookie
        String token = request.getHeaders().getFirst("token");
        if (token==null){
            HttpCookie token1 = request.getCookies().getFirst("token");
            if (token1 != null){
                token = token1.getValue();
            }
        }

        if (token != null){
            String userId = (String) redisTemplate.opsForValue().get("token");
            return userId;
        }

        return null;
    }

    private void out(ServerHttpResponse response,ResultCodeEnum resultCodeEnum) {
        //写到响应体中
        Result<Object> build = Result.build(null, resultCodeEnum);
        //将对象转换成字符串
        String result = JSONObject.toJSONString(build);
        //将result转换成二进制流
        DataBuffer wrap = response.bufferFactory().wrap(result.getBytes());
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE,"utf-8");
        response.writeWith(Mono.just(wrap));
    }

    //设置过滤器的优先级
    @Override
    public int value() {
        return 0;
    }
}
