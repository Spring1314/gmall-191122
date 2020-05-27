package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.gateway.constant.RedisConst;
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
import java.net.URI;
import java.net.URLEncoder;

/**
 * @author Administrator
 * @create 2020-05-24 14:28
 * 1.添加redis依赖
 * 2.配置redis
 * 3.添加redis配置文件
 */
@Component
//@Order(0)
public class LoginGlobalFilter implements GlobalFilter,Order{

    @Autowired
    private RedisTemplate redisTemplate;

    private AntPathMatcher antPathMatcher =new AntPathMatcher();
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
        //1.浏览器无权访问/inner的资源
        if (antPathMatcher.match("/inner/**",path)){
            return out(response,ResultCodeEnum.PERMISSION);
        }
        //获取用户ID
        String userId = getUserId(request);
        //2.浏览器访问/auth的路径，要判断用户是否登录
        if (antPathMatcher.match("/auth/**",path) && StringUtils.isEmpty(userId)){
            return out(response,ResultCodeEnum.LOGIN_AUTH);
        }

        //3.判断是否是刷新页面 ->重定向到登录页面
        for (String url : authUrl) {
            if (path.contains(url) && StringUtils.isEmpty(userId)){
                //重定向
                response.setStatusCode(HttpStatus.SEE_OTHER);
                String rawSchemeSpecificPart = request.getURI().getRawSchemeSpecificPart();
                try {
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://passport.gmall.com/login.html?originUrl="
                            + URLEncoder.encode(rawSchemeSpecificPart,"utf-8"));
                    //响应给浏览器
                    response.setComplete();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        //将userId传给后来页面，如购物车页面，从而判是谁的购物车
        //mutate()创建一个新的请求 并设置用户ID
        if (!StringUtils.isEmpty(userId)){
            request.mutate().header("userId",userId);
        }

        //获取临时用户id
        String userTempId = getUserTempId(request);
        if(!StringUtils.isEmpty(userTempId)){
            request.mutate().header("userTempId",userTempId);
        }
        //放行
        return chain.filter(exchange);
    }

    //获取临时用户id
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = request.getHeaders().getFirst("userTempId");
        if (userTempId == null){
            HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
            if (httpCookie != null){
                userTempId = httpCookie.getValue();
            }
        }
        return userTempId;
    }

    //获取用户ID
    private String getUserId(ServerHttpRequest request) {
        //获得token -> Header 或者 Cookie
        String token = request.getHeaders().getFirst("token");
        if (token == null){
            //Header中没有，去Cookie中查询
            HttpCookie token1 = request.getCookies().getFirst("token");
            if (token1 != null){
                token = token1.getValue();
            }
        }

        //通过token去缓存中获得userId
        if (!StringUtils.isEmpty(token)){
            String cacheKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
            String userId = (String) redisTemplate.opsForValue().get(cacheKey);
            return userId;
        }
        return null;
    }

    private Mono<Void> out(ServerHttpResponse response,ResultCodeEnum resultCodeEnum) {
        //写入响应体中
        Result<Object> build = Result.build(null, resultCodeEnum);
        //将build对象转换成二进制流
        String result = JSONObject.toJSONString(build);
        DataBuffer wrap = response.bufferFactory().wrap(result.getBytes());
        //中文乱码问题
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE,"application/json;charset=utf-8");
        return response.writeWith(Mono.just(wrap));
    }

    //设置过滤器的执行顺序
    @Override
    public int value() {
        return 0;
    }
}
