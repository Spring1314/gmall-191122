package com.atguigu.gmall.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 * @create 2020-05-19 20:52
 */
@RestController
@RequestMapping("/api/list")
public class ListApiController {
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @GetMapping("/inner/createIndex")
    public Result createIndex(){
        //1.创建索引库
        elasticsearchRestTemplate.createIndex(Goods.class);
        //2.创建映射关系 Mapping
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }
}
