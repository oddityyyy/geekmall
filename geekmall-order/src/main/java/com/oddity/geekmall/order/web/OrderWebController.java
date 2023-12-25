package com.oddity.geekmall.order.web;

import com.oddity.common.exception.NoStockException;
import com.oddity.geekmall.order.entity.OrderEntity;
import com.oddity.geekmall.order.service.OrderService;
import com.oddity.geekmall.order.vo.OrderConfirmVo;
import com.oddity.geekmall.order.vo.OrderSubmitVo;
import com.oddity.geekmall.order.vo.SubmitOrderResponseVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @author oddity
 * @create 2023-10-20 16:48
 */

@Controller
public class OrderWebController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderService orderService;

    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrderTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setModifyTime(new Date());
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderEntity);
        return "ok";
    }

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);

        return "confirm";
    }

    /**
     * 下单功能
     * @param vo
     * @param model
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){

        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

            if (responseVo.getCode() == 0){
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            }else {
                String msg = "下单失败:";
                switch (responseVo.getCode()){
                    case 1: msg += "订单信息过期，请刷新再次提交"; break;
                    case 2: msg += "订单商品价格发生变化，请确认后再次提交"; break;
                    case 3: msg += "库存锁定失败，商品库存不足"; break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:http://order.geekmall.com/toTrade";
            }
        } catch (Exception e) {
            if (e instanceof NoStockException){
                String message = ((NoStockException) e).getMessage();
                redirectAttributes.addFlashAttribute("msg", message);
            }
            return "redirect:http://order.geekmall.com/toTrade";
        }
    }
}
