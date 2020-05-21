package com.atguigu.gmall.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.dao.GoodsDao;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.service.ListService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2020-05-20 11:40
 */
@Service
public class ListServiceImpl implements ListService {
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private RedisTemplate redisTemplate;
    //官方推荐使用的原生客户端
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //添加索引库  商品上架
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //1.商品相关的属性
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        goods.setId(skuInfo.getId());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        //2.商品品牌相关属性
        BaseTrademark baseTrademark = productFeignClient.getBaseTrademark(skuInfo.getTmId());
        goods.setTmId(baseTrademark.getId());
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        //3.商品分类相关属性
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuId);
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());
        //4.平台属性集合
        List<SkuAttrValue> attrList = productFeignClient.getAttrList(skuInfo.getId());
        List<SearchAttr> searchAttrList = attrList.stream().map(skuAttrValue -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(skuAttrValue.getBaseAttrInfo().getId());
            searchAttr.setAttrName(skuAttrValue.getBaseAttrInfo().getAttrName());
            searchAttr.setAttrValue(skuAttrValue.getBaseAttrValue().getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrList);
        //5.时间
        goods.setCreateTime(new Date());
        goodsDao.save(goods);
    }

    //删除索引库  商品下架
    @Override
    public void lowerGoods(Long skuId) {
        goodsDao.deleteById(skuId);
    }

    //更新商品
    @Override
    public void incrHotScore(Long skuId) {

        String key = "hotScore";
        //参数1：热度，参数2：哪一款商品，参数3：追加分
        Double score = redisTemplate.opsForZSet().incrementScore(key,skuId,1);
        if (score % 10 ==0){
            Optional<Goods> optional = goodsDao.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(Math.round(score));
            goodsDao.save(goods);
        }
    }

    //查询商品
    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        //1.构建查询条件对象
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        //2.执行查询
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //3.解析查询结果
            SearchResponseVo responseVo = parseSearchResponse(searchResponse);
            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //解析查询结果
    private SearchResponseVo parseSearchResponse(SearchResponse searchResponse) {
        SearchResponseVo responseVo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits();
        //总条数
        long totalHits = hits.getTotalHits();
        responseVo.setTotal(totalHits);
        //商品集合
        SearchHit[] hits1 = hits.getHits();
        List<Goods> goodsList = Arrays.stream(hits1).map(h -> {
            String sourceAsString = h.getSourceAsString();
            //将字符串转换成对象
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);
        return responseVo;
    }

    //构建查询条件对象
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchRequest searchRequest = new SearchRequest();
        //构建资源条件对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询所有
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //指定索引库的名称，可以指定多个
        searchRequest.indices("goods");
        //指定索引库的类型，es7以后没有类型的概念，此处可写可不写
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

}
