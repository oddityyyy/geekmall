package com.oddity.geekmall.cart.controller;

import com.oddity.geekmall.cart.service.CartService;
import com.oddity.geekmall.cart.vo.Cart;
import com.oddity.geekmall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * @author oddity
 * @create 2023-10-16 21:27
 */

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems() {

        return cartService.getUserCartItems();
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){

        cartService.deleteItem(skuId);

        return "redirect:http://cart.geekmall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num){

        cartService.changeItemCount(skuId, num);

        return "redirect:http://cart.geekmall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("checked") Integer check){

        cartService.checkItem(skuId, check);

        return "redirect:http://cart.geekmall.com/cart.html";
    }

    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);

        return "cartList";
    }

    /**
     * 添加商品到购物车
     * RedirectAttributes.addFlashAttribute();将数据放在session中可以在页面中取出，但是只能去取一次
     * RedirectAttributes.addAttribute("skuId", skuId) 将数据放在url后面，可以无限取
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId); //不同于flash, 此方法直接拼接到url后面

        return "redirect:http://cart.geekmall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model){

        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItem);

        return "success";
    }
}
