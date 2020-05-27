package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    //添加购物车
    @Override
    public CartInfo addToCart(Long skuId, Integer skuNum, String userId) {
        //先去redis中查询
        String cacheKey = getCacheKey(userId);
        //一个用户可以把多件不同的商品加入购物车，所以String类型不能满足，而是使用map类型
        //CartInfo cartInfo = (CartInfo) redisTemplate.opsForValue().get(cacheKey);
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(cacheKey, skuId.toString());
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
            System.out.println(cartInfo);
            cartInfoMapper.insert(cartInfo);
        }
        //保存到缓存中
        //redisTemplate.opsForValue().set(cacheKey,cartInfo,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
        //redisTemplate.opsForHash().put(cacheKey,skuId.toString(),cartInfo);
        //setExpire(cacheKey);
        loadCartCache(userId);
        return cartInfo;
    }

    //去购物车结算页面，获得用户的购物车集合
    @Override
    public List<CartInfo> cartList(String userId, String userTempId) {
        //1.临时用户的购物车集合
        if (StringUtils.isEmpty(userId)) {
            return this.cartList(userTempId);
        } else {
            //真实用户id存在,临时用户id不存在
            //2.真实用户的购物车集合
            if (StringUtils.isEmpty(userTempId)) {
                return this.cartList(userId);
            } else {
                //真实用户id存在,临时用户id存在
                //3.真实用户和临时用户合并后的购物车集合
                return this.mergeList(userId, userTempId);
            }
        }
    }

    //更改商品选中状态
    @Override
    public void checkCart(Long skuId, Integer isChecked, String userId) {
        //修改数据库
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId).eq("sku_id",skuId);
        cartInfoMapper.update(cartInfo,wrapper);
        //修改缓存
        String cacheKey = getCacheKey(userId);
        cartInfo = (CartInfo) redisTemplate.opsForHash().get(cacheKey, skuId.toString());
        cartInfo.setIsChecked(isChecked);
        redisTemplate.opsForHash().put(cacheKey,skuId.toString(),cartInfo);

    }

    //获得选中的商品集合
    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        //先去缓存中查询
        String cacheKey = getCacheKey(String.valueOf(userId));
        List<CartInfo> cartInfoList = redisTemplate.opsForHash().values(cacheKey);
        if (!CollectionUtils.isEmpty(cartInfoList)){
            //缓存中存在，返回选中得商品集合
            cartInfoList = cartInfoList.stream().filter(cartInfo -> {
                if (cartInfo.getIsChecked().intValue() == 1){
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
        } else {
            //缓存中不存在，去数据库中查询
            QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id",userId).eq("is_checked",1);
            cartInfoList = cartInfoMapper.selectList(wrapper);
        }
        //实时价格
        cartInfoList.forEach(cartInfo -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(cartInfo.getSkuId());
            cartInfo.setSkuPrice(skuPrice);
        });
        return cartInfoList;
    }


    //临时用户或者真实用户的购物车集合
    public List<CartInfo> cartList(String userId) {
        //1.先去缓存中获得购物车集合
        String cacheKey = getCacheKey(userId);
        List<CartInfo> cartInfoList = redisTemplate.opsForHash().values(cacheKey);
        if (CollectionUtils.isEmpty(cartInfoList)){
            cartInfoList = this.loadCartCache(userId);
        }

        //实时价格
        cartInfoList.forEach(cartInfo -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(cartInfo.getSkuId());
            cartInfo.setSkuPrice(skuPrice);
        });

        //购物车排序 根据id降序
        cartInfoList.sort((o1,o2)->{
            return -(o1.getId().toString().compareTo(o2.getId().toString()));
        });
        return cartInfoList;
    }

    //导入数据库中购物车集合到缓存中
    public List<CartInfo> loadCartCache(String userId){
        String cacheKey = getCacheKey(userId);
        //2.去数据库中进行查询
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(wrapper);
        //3.如果数据库中存在购物车集合，保存到缓存中一份
        if (!CollectionUtils.isEmpty(cartInfoList)) {
            //将购物车集合转换成map
            Map<String, CartInfo> cartInfoMap = cartInfoList.stream().collect(Collectors.toMap(cartInfo -> {
                return cartInfo.getSkuId().toString();
            }, cartInfo -> {
                return cartInfo;
            }));
            //一次性连接redis,不用多次连接以及遍历购物车集合
            redisTemplate.opsForHash().putAll(cacheKey, cartInfoMap);
            //设置缓存过期时间
            setExpire(cacheKey);
            return cartInfoList;
        } else {
            return new ArrayList<>();
        }

    }

    //真实用户和临时用户合并后的购物车集合
    public List<CartInfo> mergeList(String userId,String userTempId) {
        //获得真实用户的购物车集合
        List<CartInfo> cartInfoList = cartList(userId);
        //获得临时用户的购物车集合
        List<CartInfo> cartInfoListTemp = cartList(userTempId);

        if (!CollectionUtils.isEmpty(cartInfoList)){
            if (CollectionUtils.isEmpty(cartInfoListTemp)){
                return cartInfoList;
            } else {
                //将真实用户购物车集合转换成map
                Map<String, CartInfo> cartInfoMap = cartInfoList.stream().collect(Collectors.toMap(cartInfo -> {
                    return cartInfo.getSkuId().toString();
                }, cartInfo -> {
                    return cartInfo;
                }));
                //将临时用户购物车集合合并到真实用户购物车集合中
                for (CartInfo cartInfoTemp : cartInfoListTemp) {
                    CartInfo cartInfoUser = cartInfoMap.get(cartInfoTemp.getSkuId().toString());
                    if (cartInfoUser != null){
                        //数据库中已存在，合并到数据库
                        //修改购物车中商品数量
                        cartInfoUser.setSkuNum(cartInfoUser.getSkuNum() + cartInfoTemp.getSkuNum());
                        //设置选中状态
                        if (cartInfoTemp.getIsChecked().intValue() == 1){
                            cartInfoUser.setIsChecked(1);
                        }
                        //更新购物车数据
                        cartInfoMapper.updateById(cartInfoUser);
                        //删除临时购物车数据
                        cartInfoMapper.deleteById(cartInfoTemp.getId());
                    } else {
                        //数据库中不存在，新增到数据库
                        cartInfoMap.put(cartInfoTemp.getSkuId().toString(),cartInfoTemp);
                        cartInfoTemp.setUserId(userId);
                        cartInfoMapper.updateById(cartInfoTemp);
                    }
                }
                //统一删除临时用户在缓存中得数据
                String cacheKey = getCacheKey(userId);
                String cacheKeyTemp = getCacheKey(userTempId);
                if (redisTemplate.hasKey(cacheKeyTemp)){
                    redisTemplate.delete(cacheKeyTemp);
                    redisTemplate.delete(cacheKey);
                }
                return new ArrayList<>(cartInfoMap.values());
            }
        }
        return null;
    }

    //获得缓存key
    public String getCacheKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    //设置缓存的过期时间
    public void setExpire(String cacheKey) {
        redisTemplate.expire(cacheKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }
}
