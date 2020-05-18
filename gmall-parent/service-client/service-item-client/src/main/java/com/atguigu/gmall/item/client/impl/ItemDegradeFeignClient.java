package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-16 12:32
 */
@Component
public class ItemDegradeFeignClient implements ItemFeignClient {
    @Override
    public Map<String, Object> getItem(Long skuId) {
        return null;
    }
}
