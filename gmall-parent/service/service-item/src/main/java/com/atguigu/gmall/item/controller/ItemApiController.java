package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-15 22:02
 */
@RestController
@RequestMapping("/api/item")
public class ItemApiController {
    @Autowired
    private ItemService itemService;
    @GetMapping("/getItem/{skuId}")
    public Map<String,Object> getItem(@PathVariable("skuId") Long skuId){
        return itemService.getItem(skuId);
    }
}
