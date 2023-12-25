package com.oddity.geekmall.cart.service;

import com.oddity.geekmall.cart.vo.Cart;
import com.oddity.geekmall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
@author oddity
@create 2023-10-14 20:31
*/

public interface CartService {

    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
