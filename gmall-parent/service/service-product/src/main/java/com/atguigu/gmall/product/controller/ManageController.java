package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-12 18:18
 */
//针对平台属性的操作
@RestController
@RequestMapping("/admin/product")
public class ManageController {
    @Autowired
    private ManageService manageService;
    //1.获取一级分类
    @GetMapping("/getCategory1")
    public Result getCategory1(){
        List<BaseCategory1> baseCategory1List = manageService.getCategory1();
        return Result.ok(baseCategory1List);
    }

    //2.根据一级分类id获取二级分类
    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable("category1Id") Long category1Id){
        List<BaseCategory2> baseCategory2List = manageService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    //3.根据二级分类id获取三级分类
    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable("category2Id") Long category2Id){
        List<BaseCategory3> baseCategory3List = manageService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    //4.根据一二三级分类 查询平台属性集合
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(
            @PathVariable(name = "category1Id") Long category1Id,
            @PathVariable(name = "category2Id") Long category2Id,
            @PathVariable(name = "category3Id") Long category3Id){
        List<BaseAttrInfo> baseAttrInfoList =  manageService.attrInfoList(category1Id,category2Id,category3Id);
        return Result.ok(baseAttrInfoList);
    }

    //5.添加/修改平台属性
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        if(baseAttrInfo.getId()==null){
            System.out.println("新增操作");
            manageService.saveAttrInfo(baseAttrInfo);
        } else {
            System.out.println("更新操作");
            manageService.updateAttrInfo(baseAttrInfo);
        }
        return Result.ok();
    }

    //6.根据平台属性ID获取平台属性
    @GetMapping("/getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable("attrId") Long attrId){
        List<BaseAttrValue> baseAttrValueList = manageService.getAttrValueList(attrId);
        return Result.ok(baseAttrValueList);
    }

    //获取spu分页列表
    @GetMapping("/{page}/{limit}")
    public Result selectSpuInfoByPage(@PathVariable("page") Integer page,
                                      @PathVariable("limit") Integer limit,
                                      Long category3Id){
        IPage<SpuInfo> pageInfo = manageService.selectSpuInfoByPage(page,limit,category3Id);
        return Result.ok(pageInfo);
    }

    //获取销售属性
    @GetMapping("/baseSaleAttrList")
    public Result baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = manageService.baseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    //获取品牌属性
    @GetMapping("/baseTrademark/getTrademarkList")
    public Result getTrademarkList(){
        List<BaseTrademark> baseTrademarkList = manageService.getTrademarkList();
        return Result.ok(baseTrademarkList);
    }

    //添加spu
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    //获取品牌分页列表
    @GetMapping("/baseTrademark/{page}/{limit}")
    public Result baseTrademark(@PathVariable("page") Integer page,
                                @PathVariable("limit") Integer limit){
        IPage<BaseTrademark> pageInfo = manageService.baseTrademark(page,limit);
        return Result.ok(pageInfo);
    }

    //添加品牌
    @PostMapping("/baseTrademark/save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        manageService.save(baseTrademark);
        return Result.ok();
    }

    //根据品牌id获得品牌信息
    @GetMapping("/baseTrademark/get/{id}")
    public Result get(@PathVariable("id") Long id){
        BaseTrademark baseTrademark = manageService.get(id);
        return Result.ok(baseTrademark);
    }

    //根据品牌id修改品牌信息
    @RequestMapping("/baseTrademark/update")
    public Result update(@RequestBody BaseTrademark baseTrademark){
        manageService.update(baseTrademark);
        return Result.ok();
    }

    //根据品牌id删除品牌信息
    @RequestMapping("/baseTrademark/remove/{id}")
    public Result remove(@PathVariable("id") Long id){
        manageService.remove(id);
        return Result.ok();
    }

    //根据spuId获取图片列表
    //接口	http://api.gmall.com/admin/product/spuImageList/{spuId}
    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId") Long spuId){
        List<SpuImage> spuImageList = manageService.spuImageList(spuId);
        return Result.ok(spuImageList);
    }

    //根据spuId获取销售属性
    //接口	http://api.gmall.com/admin/product/spuSaleAttrList/{spuId}
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId") Long spuId){
        List<SpuSaleAttr> spuSaleAttrList = manageService.spuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }

    //添加sku
    //接口	http://api.gmall.com/admin/product/saveSkuInfo
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    //获取sku分页列表
    //接口	http://api.gmall.com/admin/product/list/{page}/{limit}
    @GetMapping("list/{page}/{limit}")
    public Result list(@PathVariable("page") Integer page,
                       @PathVariable("limit") Integer limit){
        IPage<SkuInfo> skuInfoIPage = manageService.list(page,limit);
        return Result.ok(skuInfoIPage);
    }

    //上架
    //接口	http://api.gmall.com/admin/product/onSale/{skuId}
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){
        manageService.onSale(skuId);
        return Result.ok();
    }

    //下架
    //接口	http://api.gmall.com/admin/product/cancelSale/{skuId}
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){
        manageService.cancelSale(skuId);
        return Result.ok();
    }


}

