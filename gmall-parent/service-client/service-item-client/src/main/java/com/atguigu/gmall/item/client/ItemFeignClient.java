package com.atguigu.gmall.item.client;

import com.atguigu.gmall.item.client.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-16 12:31
 */
@FeignClient(value = "service-item",fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {
    @GetMapping("/api/item/getItem/{skuId}")
    public Map<String,Object> getItem(@PathVariable("skuId") Long skuId);
}
