package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.impl.ProductDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-15 21:55
 */
@FeignClient(value ="service-product", fallback = ProductDegradeFeignClient.class)

public interface ProductFeignClient {
    //1 根据skuId获取sku基本信息与图片信息
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

    //2 根据三级分类id获取分类信息
    @GetMapping("/api/product/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id);

    //3 根据skuId获取价格信息,价格实时查询
    @GetMapping("/api/product/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId);

    //4 根据skuId和spuId获取销售信息,标识出本商品对应的销售属性
    @GetMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(
            @PathVariable("skuId") Long skuId,
            @PathVariable("spuId") Long spuId
    );

    //5 根据spuId获得商品属性组合
    @GetMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    //获取全部分类信息
    @GetMapping("/api/product/getBaseCategoryList")
    public List<Map> getBaseCategoryList();

    //根据品牌id商品品牌相关属性
    @GetMapping("/api/product/getBaseTrademark/{tmId}")
    public BaseTrademark getBaseTrademark(@PathVariable("tmId") Long tmId);

    //根据skuId获得平台属性集合
    @GetMapping("/api/product/inner/getAttrList/{skuId}")
    public List<SkuAttrValue> getAttrList(@PathVariable("skuId") Long skuId);
}
