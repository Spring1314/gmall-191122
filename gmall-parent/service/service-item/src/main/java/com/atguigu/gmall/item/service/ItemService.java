package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-15 22:04
 */
public interface ItemService {
    Map<String, Object> getItem(Long skuId);
}
