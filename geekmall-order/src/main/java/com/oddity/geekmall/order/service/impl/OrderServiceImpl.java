package com.oddity.geekmall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.oddity.common.exception.NoStockException;
import com.oddity.common.to.SkuHasStockVo;
import com.oddity.common.to.mq.OrderTo;
import com.oddity.common.to.mq.SeckillOrderTo;
import com.oddity.common.utils.R;
import com.oddity.common.vo.MemberRespVo;
import com.oddity.geekmall.order.constant.OrderConstant;
import com.oddity.geekmall.order.entity.OrderItemEntity;
import com.oddity.geekmall.order.entity.PaymentInfoEntity;
import com.oddity.geekmall.order.enume.OrderStatusEnum;
import com.oddity.geekmall.order.feign.CartFeignService;
import com.oddity.geekmall.order.feign.MemberFeignService;
import com.oddity.geekmall.order.feign.ProductFeignService;
import com.oddity.geekmall.order.feign.WareFeignService;
import com.oddity.geekmall.order.interceptor.LoginUserInterceptor;
import com.oddity.geekmall.order.service.OrderItemService;
import com.oddity.geekmall.order.service.PaymentInfoService;
import com.oddity.geekmall.order.to.OrderCreateTo;
import com.oddity.geekmall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.oddity.geekmall.order.dao.OrderDao;
import com.oddity.geekmall.order.entity.OrderEntity;
import com.oddity.geekmall.order.service.OrderService;
import com.oddity.geekmall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    PaymentInfoService paymentInfoService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R hasStock = wareFeignService.getSkusHasStock(collect);
            List<SkuHasStockVo> data = hasStock.getData(new TypeReference<List<SkuHasStockVo>>() {});
            if (data != null){
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::isHasStock));
                confirmVo.setStocks(map);
            }
        }, executor);

        confirmVo.setIntegration(memberRespVo.getIntegration());

        //TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;
    }

    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {

        submitVoThreadLocal.set(vo);

        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        //默认给个随机值让业务不通过，因为code不初始化默认为0
        response.setCode(999);

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        if (result == 0L){
            //令牌验证失败
            response.setCode(1);
            return response;
        }else {
            //令牌验证成功
            OrderCreateTo order = createOrder();
            //验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01){
                //验价成功
                saveOrder(order);
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                //远程锁库存
                R r = wareFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0){
                    //锁成功
                    response.setOrderEntity(order.getOrder());
//                    int i = 10 / 0;
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    response.setCode(0);
                    return response;
                }else {
                    //锁失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
//                    response.setCode(3);
                }
            }else {
                //验价失败
                response.setCode(2);
                return response;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //所有订单都进来，查询当前这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            OrderEntity byId = this.getById(entity.getId());
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(byId, orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

    /**
     * 获取当前订单的支付信息
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {

        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);

        BigDecimal bigDecimal = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(bigDecimal.toString());
        payVo.setOut_trade_no(orderSn);
        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity itemEntity = order_sn.get(0);
        payVo.setSubject(itemEntity.getSkuName());
        payVo.setBody(itemEntity.getSkuAttrsVals());

        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {

        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );

        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(itemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(order_sn);

        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo vo) {

        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(infoEntity);

        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISH")){
            String outTradeNo = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
            return "success";
        }

        return "error";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrder) {
        //保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrder.getOrderSn());
        orderEntity.setMemberId(seckillOrder.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrder.getSeckillPrice().multiply(seckillOrder.getNum());
        orderEntity.setPayAmount(multiply);
        orderEntity.setCreateTime(new Date());
        orderEntity.setAutoConfirmDay(7);
        this.save(orderEntity);
        //保存订单项信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(seckillOrder.getOrderSn());
        itemEntity.setRealAmount(multiply);
        itemEntity.setSkuQuantity(seckillOrder.getNum().intValue());
        itemEntity.setSkuId(seckillOrder.getSkuId());
        orderItemService.save(itemEntity);
    }

    /**
     * 保存订单数据
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setCreateTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {

        OrderCreateTo orderCreateTo = new OrderCreateTo();

        String orderSn = IdWorker.getTimeId();

        //构建订单
        OrderEntity orderEntity = buildOrder(orderSn);

        //构建所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        //计算价、积分相关
        computePrice(orderEntity, itemEntities);

        //赋值
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(itemEntities);

        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {

        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        int integra = 0;
        int growth = 0;

        for (OrderItemEntity entity : itemEntities){
            coupon = coupon.add(entity.getCouponAmount());
            integration = integration.add(entity.getIntegrationAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            total = total.add(entity.getRealAmount());
            integra += entity.getGiftIntegration();
            growth += entity.getGiftGrowth();
        }

        //订单总额
        orderEntity.setTotalAmount(total);
        //应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        //其他优惠金额
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);
        //积分等信息
        orderEntity.setIntegration(integra);
        orderEntity.setGrowth(growth);

        orderEntity.setDeleteStatus(0); //未删除
    }

    private OrderEntity buildOrder(String orderSn) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);

        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {});

        entity.setFreightAmount(fareResp.getFare());
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());
        entity.setMemberId(fareResp.getAddress().getMemberId());

        entity.setPayType(orderSubmitVo.getPayType());
        entity.setNote(orderSubmitVo.getNote());
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);

        return entity;
    }

    /**
     * 构建所有订单项数据
     * @return
     * @param orderSn
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0){
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * 构建一个具体的订单项
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {

        OrderItemEntity itemEntity = new OrderItemEntity();

        //SPU信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {});
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setCategoryId(data.getCatalogId());

        //SKU信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());

        //积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        //价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        BigDecimal origin = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = origin.subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }

}