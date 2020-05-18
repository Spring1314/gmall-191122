package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jodd.time.TimeUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2020-05-12 18:22
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    //1.获取一级分类
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    //根据一级分类获取二级分类
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> wrapper = new QueryWrapper<>();
        wrapper.eq("category1_id",category1Id);
        List<BaseCategory2> baseCategory2List = baseCategory2Mapper.selectList(wrapper);
        return baseCategory2List;
    }

    //3.根据二级分类id获取三级分类
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> wrapper = new QueryWrapper<>();
        wrapper.eq("category2_id",category2Id);
        List<BaseCategory3> baseCategory3List = baseCategory3Mapper.selectList(wrapper);
        return baseCategory3List;
    }

    //4.根据一二三级分类 查询平台属性集合
    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {

        return baseAttrInfoMapper.attrInfoList(category1Id,category2Id,category3Id);
    }

    //添加平台属性
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //1.添加平台属性表
        baseAttrInfoMapper.insert(baseAttrInfo);
        //2.添加平台属性值表
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        attrValueList.forEach(attrValue->{
            //获取外键
            attrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(attrValue);
        });
    }

    //根据平台属性ID获取平台属性
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id",attrId);
        return baseAttrValueMapper.selectList(wrapper);
    }

    //修改平台属性
    @Override
    public void updateAttrInfo(BaseAttrInfo baseAttrInfo) {
        //修改平台属性表
        baseAttrInfoMapper.updateById(baseAttrInfo);
        //修改平台属性值表
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            if (baseAttrInfo.getId() != baseAttrValue.getAttrId()){
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }

    //获取spu分页列表
    @Override
    public IPage<SpuInfo> selectSpuInfoByPage(Integer page, Integer limit, Long category3Id) {
        Page<SpuInfo> spuInfoPage = new Page<>(page,limit);
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id",category3Id);
        return spuInfoMapper.selectPage(spuInfoPage,wrapper);
    }

    //获取销售属性
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {

        return baseSaleAttrMapper.selectList(null);
    }

    //获取品牌属性
    @Override
    public List<BaseTrademark> getTrademarkList() {
        return baseTrademarkMapper.selectList(null);
    }

    //获取品牌分页列表
    @Override
    public IPage<BaseTrademark> baseTrademark(Integer page, Integer limit) {
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page, limit);
        return baseTrademarkMapper.selectPage(baseTrademarkPage,null);
    }

    //添加spu
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //商品表
        spuInfoMapper.insert(spuInfo);
        //商品图片表
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        spuImageList.forEach(spuImage->{
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insert(spuImage);
        });
        //商品销售属性表
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        spuSaleAttrList.forEach(spuSaleAttr->{
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insert(spuSaleAttr);
            //商品销售属性值表
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            spuSaleAttrValueList.forEach(spuSaleAttrValue->{
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            });
        });
    }

    //根据spuId获取图片列表
    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        QueryWrapper<SpuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id",spuId);
        return spuImageMapper.selectList(wrapper);
    }

    //根据spuId获取销售属性
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.spuSaleAttrList(spuId);
    }

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    //添加sku
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //商品库存表
        skuInfoMapper.insert(skuInfo);
        //商品库存图片表
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        skuImageList.forEach(skuImage -> {
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insert(skuImage);
        });
        //商品库存与销售平台属性关联表
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        skuAttrValueList.forEach(skuAttrValue -> {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insert(skuAttrValue);
        });
        //商品库存与销售平台属性值关联表
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        });
    }

    //添加品牌
    @Override
    public void save(BaseTrademark baseTrademark) {
        baseTrademarkMapper.insert(baseTrademark);
    }

    //根据品牌id获得品牌信息
    @Override
    public BaseTrademark get(Long id) {
        return baseTrademarkMapper.selectById(id);
    }

    //根据品牌id修改品牌信息
    @Override
    public void update(BaseTrademark baseTrademark) {
        baseTrademarkMapper.updateById(baseTrademark);
    }

    //根据品牌id删除品牌信息
    @Override
    public void remove(Long id) {
        baseTrademarkMapper.deleteById(id);
    }

    //获取sku分页列表
    @Override
    public IPage<SkuInfo> list(Integer page, Integer limit) {
        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        return skuInfoMapper.selectPage(skuInfoPage,null);
    }

    //上架
    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    //下架
    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    //1 根据skuId获取sku基本信息与图片信息
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        String cashKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
        //先去redis中查询
        SkuInfo skuInfo = (SkuInfo)redisTemplate.opsForValue().get(cashKey);
        //如果存在，直接返回
        if (skuInfo != null){
            return skuInfo;
        } else { //不存在就去数据库进行查询
            RLock lock = redissonClient.getLock(lockKey);
            //缓存击穿，加锁
            try{
                boolean isLock = lock.tryLock(1, 2, TimeUnit.SECONDS);
                if (isLock){
                    skuInfo = skuInfoMapper.selectById(skuId);
                    //解决缓存穿透
                    if(skuInfo == null){
                        skuInfo = new SkuInfo();//空结果
                        redisTemplate.opsForValue().set(cashKey,skuInfo,5,TimeUnit.MINUTES);
                    } else {
                        List<SkuImage> skuImages = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuInfo.getId()));
                        skuInfo.setSkuImageList(skuImages);
                        //存放到数据库中一份
                        //缓存雪崩
                        Random random = new Random();
                        int time = random.nextInt(300);
                        redisTemplate.opsForValue().set(cashKey,skuInfo,RedisConst.SKUKEY_TIMEOUT + time,TimeUnit.SECONDS);
                    }
                } else {
                    Thread.sleep(1000);
                    return (SkuInfo) redisTemplate.opsForValue().get(cashKey);
                }
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        return skuInfo;
    }

    private SkuInfo getSkuInfoByRedis(Long skuId) {
        String cashKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
        //先从redis中查询
        SkuInfo skuInfo = (SkuInfo)redisTemplate.opsForValue().get(cashKey);
        //如果redis中存在，直接从redis中获得
        if (skuInfo != null){
            return skuInfo;
        } else { //从数据库中进行查询
            //解决额redis缓存击穿,进行加锁操作，setnx
            String uuid = UUID.randomUUID().toString();
            Boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, 1, TimeUnit.MINUTES);
            if (isLock){ //加锁了
                skuInfo = skuInfoMapper.selectById(skuId);
                //redis缓存穿透
                if (skuInfo == null){
                    skuInfo = new SkuInfo(); //空结果
                    redisTemplate.opsForValue().set(cashKey,skuInfo,5,TimeUnit.MINUTES);
                } else {
                    List<SkuImage> skuImages = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuInfo.getId()));
                    skuInfo.setSkuImageList(skuImages);
                    //保存一份到redis中
                    //解决redis缓存雪崩，过期时间添加一个随机时间
                    Random random = new Random();
                    int time = random.nextInt(300);
                    redisTemplate.opsForValue().set(cashKey,skuInfo,RedisConst.SKUKEY_TIMEOUT + time,TimeUnit.SECONDS);
                    //删除锁，要防止误删，且要保证原子性操作
                    /*String uu = (String)redisTemplate.opsForValue().get(lockKey);
                    if (!StringUtils.isEmpty(uu) && uuid.equals(uu)){
                        redisTemplate.delete(lockKey);
                    }*/
                }
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return tostring(redis.call('del',KEYS[1])) else return 0 end";
                this.redisTemplate.execute(new DefaultRedisScript<>(script), Collections.singletonList("lockKey" + ""), uuid);
            } else { //已经加锁了,其他的请求直接到redis查询
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuInfo(skuId);
            }
        }

        return skuInfo;
    }

    //2 根据三级分类id获取分类信息
    @Override
    @GmallCache(prefix = "getCategoryView")
    public BaseCategoryView getCategoryView(Long category3Id) {
        /*String cashKey = RedisConst.SKUKEY_PREFIX + category3Id + RedisConst.SKUKEY_SUFFIX;
        String lockKey = RedisConst.SKUKEY_PREFIX + category3Id + RedisConst.SKULOCK_SUFFIX;
        //先去redis中进行查询
        BaseCategoryView baseCategoryView = (BaseCategoryView)redisTemplate.opsForValue().get(cashKey);
        //如果redis中存在
        if (baseCategoryView != null){
            return baseCategoryView;
        } else { //如果不存在去数据库中进行查询
            //解决缓存击穿
            RLock lock = redissonClient.getLock(lockKey);
            try {
                boolean isLock = lock.tryLock(1, 2, TimeUnit.SECONDS);
                if (isLock){  //是第一个请求，拿到了锁
                    baseCategoryView = baseCategoryViewMapper.selectById(category3Id);
                    //解决缓存穿透
                    if(baseCategoryView == null){
                        baseCategoryView = new BaseCategoryView();
                        redisTemplate.opsForValue().set(cashKey,baseCategoryView,5,TimeUnit.MINUTES);
                    } else {
                        //保存数据库中一份
                        //解决缓存雪崩
                        Random random = new Random();
                        int time = random.nextInt(300);
                        redisTemplate.opsForValue().set(cashKey,baseCategoryView,RedisConst.SKUKEY_TIMEOUT + time,TimeUnit.SECONDS);
                    }
                } else {
                    //已经加锁了
                    Thread.sleep(1000);
                    return (BaseCategoryView) redisTemplate.opsForValue().get(cashKey);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        return baseCategoryView;*/
        return baseCategoryViewMapper.selectById(category3Id);
    }

    //3 根据skuId获取价格信息,价格实时查询
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        return skuInfo.getPrice();
    }

    //4 根据skuId和spuId获取销售信息,标识出本商品对应的销售属性
    @Override
    @GmallCache(prefix = "getSpuSaleAttrListCheckBySku")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        /*String cashKey = RedisConst.SKUKEY_PREFIX + skuId + spuId + RedisConst.SKUKEY_SUFFIX;
        String lockKey = RedisConst.SKUKEY_PREFIX + skuId + spuId + RedisConst.SKULOCK_SUFFIX;
        //先去redis中进行查询
        List<SpuSaleAttr> spuSaleAttrList = (List<SpuSaleAttr>)redisTemplate.opsForValue().get(cashKey);
        //如果缓存中存在，直接返回
        if (spuSaleAttrList != null *//*&& !spuSaleAttrList.isEmpty()*//*){
            return spuSaleAttrList;
        } else { //去数据库中进行查询
            //解决缓存击穿
            RLock lock = redissonClient.getLock(lockKey);
            try {
                boolean isLock = lock.tryLock(1, 2, TimeUnit.SECONDS);
                if (isLock){ //是第一个请求，拿到了锁
                    spuSaleAttrList =
                    //解决缓存穿透问题
                    if (spuSaleAttrList == null){
                        spuSaleAttrList = new ArrayList<SpuSaleAttr>();
                        redisTemplate.opsForValue().set(cashKey,spuSaleAttrList,5,TimeUnit.MINUTES);
                    } else {
                        //保存到数据库中一份
                        //解决缓存雪崩问题
                        Random random = new Random();
                        int time = random.nextInt(300);
                        redisTemplate.opsForValue().set(cashKey,spuSaleAttrList,RedisConst.SKUKEY_TIMEOUT + time,TimeUnit.SECONDS);
                    }
                } else {  //不是第一个请求，访问redis
                    Thread.sleep(1000);
                    return (List<SpuSaleAttr>)redisTemplate.opsForValue().get(cashKey);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        return spuSaleAttrList;*/
        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    //5 根据skuId获得商品属性组合
    @Override
    @GmallCache(prefix = "getSkuValueIdsMap")
    public Map getSkuValueIdsMap(Long spuId) {
        /*String cashKey = RedisConst.SKUKEY_PREFIX + spuId + RedisConst.SKUKEY_SUFFIX;
        String lockKey = RedisConst.SKUKEY_PREFIX + spuId + RedisConst.SKULOCK_SUFFIX;
        //先去redis中获取
        Map resultMap = (Map)redisTemplate.opsForValue().get(cashKey);
        //如果缓存中存在，直接返回
        if (resultMap != null){
            return resultMap;
        } else {
            //缓存中不存在，去数据库中进行查询
            //解决缓存击穿问题
            RLock lock = redissonClient.getLock(lockKey);
            try {
                boolean isLock = lock.tryLock(1, 2, TimeUnit.SECONDS);
                if (isLock){
                    //是第一个请求，拿到了锁
                    List<Map> skuValueIdsMap = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
                    for (Map map : skuValueIdsMap) {
                        resultMap.put(map.get("value_ids"), map.get("sku_id"));
                    }
                    //解决缓存穿透问题
                    if (resultMap == null){
                        resultMap = new HashMap();
                        redisTemplate.opsForValue().set(cashKey,resultMap,5,TimeUnit.MINUTES);
                    } else {
                        //解决缓存雪崩
                        Random random = new Random();
                        int time = random.nextInt(300);
                        redisTemplate.opsForValue().set(cashKey,resultMap,RedisConst.SKUKEY_TIMEOUT + time,TimeUnit.SECONDS);
                    }
                } else {
                    //其他请求，去redis中获取
                    return (Map)redisTemplate.opsForValue().get(cashKey);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
       return resultMap;*/
        Map resultMap = new HashMap();
        List<Map> skuValueIdsMap = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        skuValueIdsMap.forEach(map -> {
            resultMap.put(map.get("value_ids"),map.get("sku_id"));
        });
        return resultMap;
    }


}
