package com.atguigu.gmall.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.service.ListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author Administrator
 * @create 2020-05-19 20:52
 * 商品搜索列表接口
 */
@RestController
@RequestMapping("/api/list")
public class ListApiController {
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private ListService listService;

    @GetMapping("/inner/createIndex")
    public Result createIndex(){
        //1.创建索引库
        elasticsearchRestTemplate.createIndex(Goods.class);
        //2.创建映射关系 Mapping
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    //添加索引库  商品上架
    @GetMapping("/inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId){
        listService.upperGoods(skuId);
        return Result.ok();
    }

    //删除索引库  商品下架
    @GetMapping("/inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId){
        listService.lowerGoods(skuId);
        return Result.ok();
    }

    //更新商品
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId) {
        listService.incrHotScore(skuId);
        return Result.ok();
    }

    //查询商品
    @PostMapping
    public SearchResponseVo list(@RequestBody SearchParam searchParam){
        return listService.list(searchParam);
    }
}
