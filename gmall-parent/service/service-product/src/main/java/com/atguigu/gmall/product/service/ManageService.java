package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-12 18:22
 */
public interface ManageService {

    List<BaseCategory1> getCategory1();


    List<BaseCategory2> getCategory2(Long category1Id);

    List<BaseCategory3> getCategory3(Long category2Id);

    List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(Long attrId);

    void updateAttrInfo(BaseAttrInfo baseAttrInfo);

    IPage<SpuInfo> selectSpuInfoByPage(Integer page, Integer limit, Long category3Id);

    List<BaseSaleAttr> baseSaleAttrList();

    List<BaseTrademark> getTrademarkList();

    IPage<BaseTrademark> baseTrademark(Integer page, Integer limit);

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuImage> spuImageList(Long spuId);

    List<SpuSaleAttr> spuSaleAttrList(Long spuId);

    void saveSkuInfo(SkuInfo skuInfo);

    void save(BaseTrademark baseTrademark);

    BaseTrademark get(Long id);

    void update(BaseTrademark baseTrademark);

    void remove(Long id);

    IPage<SkuInfo> list(Integer page, Integer limit);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    SkuInfo getSkuInfo(Long skuId);

    BaseCategoryView getCategoryView(Long category3Id);

    BigDecimal getSkuPrice(Long skuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    Map getSkuValueIdsMap(Long spuId);

    List<Map> getBaseCategoryList();
}

