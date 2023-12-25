package com.oddity.geekmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.oddity.common.to.mq.SeckillOrderTo;
import com.oddity.common.utils.R;
import com.oddity.common.vo.MemberRespVo;
import com.oddity.geekmall.seckill.feign.CouponFeignService;
import com.oddity.geekmall.seckill.feign.ProductFeignService;
import com.oddity.geekmall.seckill.interceptor.LoginUserInterceptor;
import com.oddity.geekmall.seckill.service.SeckillService;
import com.oddity.geekmall.seckill.to.SecKillSkuRedisTo;
import com.oddity.geekmall.seckill.vo.SeckillSessionWithSkus;
import com.oddity.geekmall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author oddity
 * @create 2023-10-28 17:57
 */

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; //+商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1. 扫描最近三天需要参与秒杀的活动
        R session = couponFeignService.getLate3DaySession();
        if (session.getCode() == 0){
            List<SeckillSessionWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSessionWithSkus>>(){});
            if(sessionData != null){
                //缓存活动信息
                saveSessionInfos(sessionData);
                //缓存活动关联的商品信息
                saveSessionSkuInfos(sessionData);
            }
        }
    }

    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {

        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys){
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time >= start && time <= end){
                List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                BoundHashOperations<String, String, Object> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<Object> list = hashOps.multiGet(range);
                if (list != null){
                    List<SecKillSkuRedisTo> collect = list.stream().map(item -> {
                        SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject((String) item, SecKillSkuRedisTo.class);
                        return secKillSkuRedisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0){
            String regx = "\\d_" + skuId;
            for(String key : keys){
                if (Pattern.matches(regx, key)){
                    String json = hashOps.get(key);
                    SecKillSkuRedisTo skuRedisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
                    long current = new Date().getTime();
                    if (current >= skuRedisTo.getStartTime() && current <= skuRedisTo.getEndTime()){
                        //保留随机码
                    }else {
                        skuRedisTo.setRandomCode(null);
                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {

        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        String json = hashOps.get(killId);

        if (StringUtils.isEmpty(json)){
            return null;
        }else {
            SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long time = new Date().getTime();
            long ttl = endTime - startTime;
            if (time >= startTime && time <= endTime){
                String randomCode = redisTo.getRandomCode();
                String id = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
                if (randomCode.equals(key) && id.equals(killId)){
                    if (num <= redisTo.getSeckillLimit().intValue()){
                        String redisKey = respVo.getId() + "_" + id;
                        Boolean isSuccess = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (isSuccess) {
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + key);
                            try {
                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                if(b){
                                    //秒杀成功 快速下单 发MQ消息
                                    String timeId = IdWorker.getTimeId();
                                    SeckillOrderTo orderTo = new SeckillOrderTo();
                                    orderTo.setOrderSn(timeId);
                                    orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                    orderTo.setMemberId(respVo.getId());
                                    orderTo.setNum(new BigDecimal(num.toString()));
                                    orderTo.setSkuId(orderTo.getSkuId());
                                    orderTo.setPromotionSessionId(orderTo.getPromotionSessionId());
                                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                    return timeId;
                                }else {
                                    return null;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }else {
                            return null;
                        }
                    }else {
                        return null;
                    }
                }else {
                    return null;
                }
            }else {
                return null;
            }
        }
    }

    private void saveSessionInfos(List<SeckillSessionWithSkus> sessions){
        sessions.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = redisTemplate.hasKey(key);
            if (!hasKey){
                List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionWithSkus> sessions){
        sessions.stream().forEach(session -> {
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {

                String token = UUID.randomUUID().toString().replace("-", "");

                if (!ops.hasKey(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString())){
                    //缓存商品
                    SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                    // sku基本数据
                    R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (skuInfo.getCode() == 0) {
                        SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                        redisTo.setSkuInfo(info);
                    }
                    // sku秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo, redisTo);
                    // 秒杀时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    //设置随机码
                    redisTo.setRandomCode(token);
                    String jsonString = JSON.toJSONString(redisTo);
                    ops.put(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString(), jsonString);
                    //设置信号量
                    if (!redisTemplate.hasKey(SKU_STOCK_SEMAPHORE + token)){
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                        semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());
                    }
                }
            });
        });
    }
}
