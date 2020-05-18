package com.atguigu.gmall.common.cache;

import com.atguigu.gmall.common.constant.RedisConst;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2020-05-18 11:36
 * 自定义缓存切面
 */
@Aspect
@Component
@Slf4j  //打印日志文件到控制台
public class GmallCacheAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Around(value = "@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object getRedisCache(ProceedingJoinPoint joinPoint){
        //获取入参
        Object[] args = joinPoint.getArgs();
        //获取方法的返回值类型
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        Class returnType = method.getReturnType();
        //获取方法的前缀
        String prefix = method.getAnnotation(GmallCache.class).prefix();
        String cacheKey = prefix + ":" + Arrays.asList(args).toString();
        //1.先去redis中查询
        Object o = redisTemplate.opsForValue().get(cacheKey);
        if ( o != null){
            //2.缓存中存在，直接返回
            log.info("缓存中存在此数据，无需去数据库中进行查询");
            return o;
        } else {
            //3.缓存中不存在，去数据库中进行查询
            //解决缓存击穿问题
            String lockKey = cacheKey + RedisConst.SKULOCK_SUFFIX;
            RLock lock = redissonClient.getLock(lockKey);
            try {
                boolean isLock = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (isLock){
                    log.info("拿到了锁，开始查询数据");
                    Object result = joinPoint.proceed(args);
                    //解决缓存穿透问题
                    if (result == null){
                        result = returnType.newInstance();  //空结果
                        redisTemplate.opsForValue().set(cacheKey,result,5,TimeUnit.MINUTES);
                    } else {
                        //解决缓存雪崩
                        Random random = new Random();
                        int time = random.nextInt(300);
                        redisTemplate.opsForValue().set(cacheKey,result,RedisConst.SKUKEY_TIMEOUT + time,TimeUnit.SECONDS);
                    }
                    return result;
                } else {
                    log.info("获取锁失败，从缓存中获取");
                    return  redisTemplate.opsForValue().get(cacheKey);
                }
            } catch (Throwable e) {
                //e.printStackTrace();
                log.info("程序出现异常:{}",e.getMessage());
            } finally {
                log.info("手动释放锁");
                lock.unlock();
            }
        }
        return null;
    }
}
