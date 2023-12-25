package com.oddity.geekmall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.oddity.common.exception.NoStockException;
import com.oddity.common.to.mq.OrderTo;
import com.oddity.common.to.mq.StockDetailTo;
import com.oddity.common.to.mq.StockLockedTo;
import com.oddity.common.utils.R;
import com.oddity.geekmall.ware.entity.WareOrderTaskDetailEntity;
import com.oddity.geekmall.ware.entity.WareOrderTaskEntity;
import com.oddity.geekmall.ware.feign.OrderFeignService;
import com.oddity.geekmall.ware.feign.ProductFeignService;
import com.oddity.geekmall.ware.service.WareOrderTaskDetailService;
import com.oddity.geekmall.ware.service.WareOrderTaskService;
import com.oddity.geekmall.ware.vo.OrderItemVo;
import com.oddity.geekmall.ware.vo.OrderVo;
import com.oddity.geekmall.ware.vo.SkuHasStockVo;
import com.oddity.geekmall.ware.vo.WareSkuLockVo;
import com.oddity.geekmall.ware.dao.WareSkuDao;
import com.oddity.geekmall.ware.entity.WareSkuEntity;
import com.oddity.geekmall.ware.service.WareSkuService;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;

import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId){
        //库存解锁
        wareSkuDao.unlockStock(skuId, wareId, num);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2); //变为已解锁
        wareOrderTaskDetailService.updateById(entity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //判断如果还没有这条库存记录则新增
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities == null || wareSkuEntities.size() == 0){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);

            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
            }catch (Exception e){

            }

            wareSkuDao.insert(wareSkuEntity);
        }else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());

        return collect;
    }

    /**
     * 为某个订单锁定库存
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public boolean orderLockStock(WareSkuLockVo vo) {

        //保存库存工作单的详情
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStcok> collect = locks.stream().map(item -> {
            SkuWareHasStcok stock = new SkuWareHasStcok();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        //锁定库存
        for (SkuWareHasStcok hasStcok : collect){
            Boolean skuStocked = false;
            Long skuId = hasStcok.getSkuId();
            List<Long> wareIds = hasStcok.getWareId();
            if (wareIds == null && wareIds.size() == 0){
                throw new NoStockException(hasStcok.getSkuId());
            }
            for (Long wareId : wareIds){
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStcok.getNum());
                if (count == 1){
                    skuStocked = true;
                    //告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", hasStcok.getNum(), taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(entity);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, detailTo);
                    lockedTo.setDetailTo(detailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                }else {
                    //当前仓库锁失败，重试下一个仓库
                }
            }
            if (skuStocked == false){
                throw new NoStockException(skuId);
            }
        }

        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        //查询数据库关于此订单的锁定库存信息
        //有：锁库存是成功的
        //  解锁：订单情况
        //      1. 没有这个订单，必须解锁
        //      2. 有这个订单
        //          订单状态：已取消：解锁库存
        //                   没取消：不能解锁
        //没有：库存锁定失败了，库存回滚了。无需解锁
        Long id = to.getId();
        StockDetailTo detailTo = to.getDetailTo();
        Long detailToId = detailTo.getId();
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailToId);
        if (byId != null){//有可能因为网络抖动
            //解锁逻辑
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0){
                OrderVo data = r.getData("data", new TypeReference<OrderVo>(){});
                if (data == null || data.getStatus() == 4){
                    //订单状态已取消 或 订单不存在(消息消费成功)
                    if (byId.getLockStatus() == 1){
                        unLockStock(detailTo.getSkuId(), detailTo.getWareId(), detailTo.getSkuNum(), detailToId);
                    }
                }
            }else {//机器问题网络抖动，拒绝消息，重新入队，让别人继续消费解锁()(消息消费失败)
                throw new RuntimeException("远程服务失败");
            }
        }else {
            //无需解锁(消息消费成功)
        }
    }

    //防止订单服务卡顿，导致订单状态消息一直改不了，库存消息优先到期。查出订单状态为新建状态，跳过解锁操作。
    //导致卡顿的订单，永远无法解锁库存。
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity task = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }

    @Data
    class SkuWareHasStcok {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }
}