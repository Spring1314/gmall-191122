package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-14 11:58
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {

    //根据skuId获得平台属性集合
    List<SkuAttrValue> getAttrList(Long skuId);
}
