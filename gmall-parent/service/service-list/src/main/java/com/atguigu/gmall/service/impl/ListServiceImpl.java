package com.atguigu.gmall.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.dao.GoodsDao;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.service.ListService;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
            /*private Long total;//总记录数
            private Integer pageSize;//每页显示的内容
            private Integer pageNo;//当前页面
            private Long totalPages;*/
            responseVo.setPageNo(searchParam.getPageNo());
            responseVo.setPageSize(searchParam.getPageSize());
            long totalPages = (responseVo.getTotal()+searchParam.getPageSize()-1)/searchParam.getPageSize();
            responseVo.setTotalPages(totalPages);
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
        //1.总条数
        long totalHits = hits.getTotalHits();
        responseVo.setTotal(totalHits);
        
        //2.商品集合
        SearchHit[] hits1 = hits.getHits();
        List<Goods> goodsList = Arrays.stream(hits1).map(h -> {
            String sourceAsString = h.getSourceAsString();
            //将字符串转换成对象
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            HighlightField title = h.getHighlightFields().get("title");
            if (title != null){
                String s = title.fragments()[0].toString();
                goods.setTitle(s);
            }
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);
        
        //3.解析品牌属性 List<SearchResponseTmVo> trademarkList
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) searchResponse.getAggregations().asMap().get("tmIdAgg");
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo tmVo = new SearchResponseTmVo();
            //3.1品牌Id
            tmVo.setTmId(Long.parseLong(bucket.getKeyAsString()));
            //3.2品牌属性
            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
            tmVo.setTmName(tmNameAgg.getBuckets().get(0).getKeyAsString());
            //3.3品牌logo
            ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
            tmVo.setTmLogoUrl(tmLogoUrlAgg.getBuckets().get(0).getKeyAsString());
            return tmVo;
        }).collect(Collectors.toList());
        responseVo.setTrademarkList(trademarkList);
        
        //4.解析平台属性 List<SearchResponseAttrVo> attrsList
        ParsedNested attrsAgg = (ParsedNested) searchResponse.getAggregations().asMap().get("attrsAgg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> responseAttrVoList = attrIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
            //4.1平台属性ID
            attrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            //4.2平台属性名称
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            //4.3平台属性值名称
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            List<String> attrValueList = attrValueAgg.getBuckets().
                    stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValueList(attrValueList);
            return attrVo;
        }).collect(Collectors.toList());
        responseVo.setAttrsList(responseAttrVoList);
        return responseVo;
    }

    //构建查询条件对象
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchRequest searchRequest = new SearchRequest();
        //构建资源条件对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //组合条件对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.关键字  必须不为空
        String keyword = searchParam.getKeyword();
        if(!StringUtils.isEmpty(keyword)){
            boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword));
        }else{
            //匹配所有
        }
        //searchSourceBuilder.query(QueryBuilders.matchAllQuery());//查询所有 没有条件

        //2.一二三级分类ID
        Long category1Id = searchParam.getCategory1Id();
        if (category1Id != null){
            //精确查询  filter在这和must效果一样，默认和组合条件的第一个保持一致
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",category1Id));
        }
        Long category2Id = searchParam.getCategory2Id();
        if(null != category2Id){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id",category2Id));
        }
        Long category3Id = searchParam.getCategory3Id();
        if(null != category3Id){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id",category3Id));
        }

        //3.品牌  trademark=2:华为
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
            String[] t = trademark.split(":");
            boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",t[0]));
        }

        //4.平台属性  props=23:4G:运行内存
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0){
            for (String prop : props) {
                String[] p = prop.split(":");
                //由于attrs是一个嵌套属性，只能组合attrs，不能组合attrs里面的数据，所有要将attrs里面的数据进行组合
                BoolQueryBuilder subBoolQueryBuilder = QueryBuilders.boolQuery();
                //平台属性ID
                subBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",p[0]));
                //平台属性值
                subBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue",p[1]));
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs",subBoolQueryBuilder,ScoreMode.None));
            }
        }
        //给条件资源对象设置组合条件对象
        searchSourceBuilder.query(boolQueryBuilder);

        //5.排序 1：综合排序/热点  2：价格
        String order = searchParam.getOrder();
        if(!StringUtils.isEmpty(order)){
            String[] o = order.split(":");
            String fieldName = "";
            switch (o[0]){
                case "1":fieldName="hotScore";break;
                case "2":fieldName="price";break;
            }
            searchSourceBuilder.sort(fieldName,o[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC);
        } else {
            searchSourceBuilder.sort("hotScore",SortOrder.DESC);
        }

        //6.分页
        Integer pageNo = searchParam.getPageNo();
        Integer pageSize = searchParam.getPageSize();
        searchSourceBuilder.from((pageNo - 1) * pageSize);
        searchSourceBuilder.size(pageSize);

        //7.高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").preTags("<font color='red'>").postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //8.分组
        //8.1品牌属性分组
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                           .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                           .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));
        //8.2平台属性分组
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrsAgg","attrs")
                           .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                           .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                           .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //指定索引库的名称，可以指定多个
        searchRequest.indices("goods");
        //指定索引库的类型，es7以后没有类型的概念，此处可写可不写
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

}
