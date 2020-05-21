package com.atguigu.gmall.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

/**
 * @author Administrator
 * @create 2020-05-20 11:40
 */
public interface ListService {
    void upperGoods(Long skuId);

    void lowerGoods(Long skuId);

    void incrHotScore(Long skuId);

    SearchResponseVo list(SearchParam searchParam);
}
