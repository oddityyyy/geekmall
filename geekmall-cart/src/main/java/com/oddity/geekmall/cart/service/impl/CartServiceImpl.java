package com.oddity.geekmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.oddity.common.utils.R;
import com.oddity.geekmall.cart.feign.ProductFeignService;
import com.oddity.geekmall.cart.interceptor.CartInterceptor;
import com.oddity.geekmall.cart.service.CartService;
import com.oddity.geekmall.cart.vo.Cart;
import com.oddity.geekmall.cart.vo.CartItem;
import com.oddity.geekmall.cart.vo.SkuInfoVo;
import com.oddity.geekmall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author oddity
 * @create 2023-10-14 20:31
 */

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final String CART_PREFIX = "geekmall:cart:";

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());

        if (StringUtils.isEmpty(res)){
            CartItem cartItem = new CartItem();

            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuinfo = productFeignService.getSkuinfo(skuId);
                SkuInfoVo data = skuinfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            }, executor);

            CompletableFuture<Void> getSkuSaleAttrValuesTask = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValuesTask).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);

            return cartItem;
        }else {
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));

            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(str, CartItem.class);
        return cartItem;
    }

    /**
     * 获取整个购物车
     * @return
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null){
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null){
                for (CartItem item : tempCartItems){
                    addToCart(item.getSkuId(), item.getCount());
                }
                clearCart(tempCartKey);
            }
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }else {
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1 ? true : false);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null){
            return null;
        }else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);

            List<CartItem> collect = cartItems.stream().filter(item -> item.getCheck()).map(item -> {
                BigDecimal price = productFeignService.getPrice(item.getSkuId());
                item.setPrice(price);
                return item;
            }).collect(Collectors.toList());

            return collect;
        }
    }

    /**
     * 获取到要操作的购物车
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null){
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        }else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

    private List<CartItem> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0){
            List<CartItem> collect = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }
}
