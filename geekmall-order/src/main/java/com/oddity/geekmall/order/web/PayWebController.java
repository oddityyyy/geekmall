package com.oddity.geekmall.order.web;

import com.alipay.api.AlipayApiException;
import com.oddity.geekmall.order.config.AlipayTemplate;
import com.oddity.geekmall.order.service.OrderService;
import com.oddity.geekmall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author oddity
 * @create 2023-10-26 20:18
 */

@Controller
public class PayWebController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        //将此页面直接交给浏览器
        String pay = alipayTemplate.pay(payVo);
        return pay;
    }
}
