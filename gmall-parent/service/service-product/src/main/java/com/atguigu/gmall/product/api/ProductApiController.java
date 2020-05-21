package com.atguigu.gmall.product.api;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-15 16:15
 * 对外暴露的接口，供商品页面详情微服务远程调用
 */
@RestController
@RequestMapping("/api/product")
public class ProductApiController {
    @Autowired
    private ManageService manageService;
    //1 根据skuId获取sku基本信息与图片信息
    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        return skuInfo;
    }

    //2 根据三级分类id获取分类信息
    @GetMapping("/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id){

        return manageService.getCategoryView(category3Id);
    }


    //3 根据skuId获取价格信息,价格实时查询
    @GetMapping("/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId){
        return manageService.getSkuPrice(skuId);
    }

    //4 根据skuId和spuId获取销售信息,标识出本商品对应的销售属性
    @GetMapping("/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId,
                                                          @PathVariable("spuId") Long spuId){

        return manageService.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    //5 根据skuId获得商品属性组合
    @GetMapping("/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId){
        return manageService.getSkuValueIdsMap(spuId);
    }

    //获取全部分类信息
    @GetMapping("/getBaseCategoryList")
    public List<Map> getBaseCategoryList(){

        return manageService.getBaseCategoryList();
    }

    //根据品牌id商品品牌相关属性
    @GetMapping("/getBaseTrademark/{tmId}")
    public BaseTrademark getBaseTrademark(@PathVariable("tmId") Long tmId){
        return manageService.getBaseTrademark(tmId);
    }

    //根据skuId获得平台属性集合
    @GetMapping("/inner/getAttrList/{skuId}")
    public List<SkuAttrValue> getAttrList(@PathVariable("skuId") Long skuId){
        return manageService.getAttrList(skuId);
    }
}
