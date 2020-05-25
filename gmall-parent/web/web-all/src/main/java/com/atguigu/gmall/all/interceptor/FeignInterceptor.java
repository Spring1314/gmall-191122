package com.atguigu.gmall.all.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author Administrator
 * @create 2020-05-25 18:41
 * 远程调用拦截器
 */
@Component
public class FeignInterceptor implements RequestInterceptor {
    //requestTemplate远程调用请求对象
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null){
            HttpServletRequest request = servletRequestAttributes.getRequest();
            if (request != null){
                String userId = request.getHeader("userId");
                if (!StringUtils.isEmpty(userId)){
                    requestTemplate.header("userId",userId);
                }
                String userTempId = request.getHeader("userTempId");
                if (!StringUtils.isEmpty(userTempId)){
                    requestTemplate.header("userTempId",userTempId);
                }
            }
        }

    }
}

