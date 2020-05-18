package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-16 12:35
 * 返回给前端页面，不能返回json串
 */
@Controller
public class WebAllController {
    @Autowired
    private ItemFeignClient itemFeignClient;
    @RequestMapping("/{skuId}.html")
    public String getItem(@PathVariable("skuId") Long skuId, Model model){
        Map<String, Object> result = itemFeignClient.getItem(skuId);
        model.addAllAttributes(result);
        return "item/index";
    }
}
