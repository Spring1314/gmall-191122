package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.CachedRowSet;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2020-05-25 18:31
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public CartInfo addCart(Long skuId, Integer skuNum, String userId) {
        //先去redis中查询
        String cacheKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForValue().get(cacheKey);
        if (cartInfo == null){
            //如果缓存中不存在去数据库中查询
            QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("sku_id",skuId).eq("user_id",userId);
            cartInfo = cartInfoMapper.selectOne(wrapper);
        }

        if (cartInfo != null){
            //此商品已存在，进行更新操作
            //更新实时价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            cartInfo.setSkuPrice(skuPrice);
            //更新商品的数量
            cartInfo.setSkuNum(skuNum + cartInfo.getSkuNum());
            //默认选中状态
            cartInfo.setIsChecked(1);
            cartInfoMapper.updateById(cartInfo);
        } else {
            //此商品不存在，进行插入操作
            cartInfo = new CartInfo();
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            //实时价格
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setIsChecked(1);
            cartInfoMapper.insert(cartInfo);
        }
        //保存到缓存中
        redisTemplate.opsForValue().set(cacheKey,cartInfo,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
        return cartInfo;
    }
}
