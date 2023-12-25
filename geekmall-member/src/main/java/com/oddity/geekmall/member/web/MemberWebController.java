package com.oddity.geekmall.member.web;

import com.alibaba.fastjson.JSON;
import com.oddity.common.utils.R;
import com.oddity.geekmall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

/**
 * @author oddity
 * @create 2023-10-26 21:21
 */

@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, Model model) {

        HashMap<String, Object> page = new HashMap<>();
        page.put("page", pageNum);
        R r = orderFeignService.listWithItem(page);
        System.out.println(JSON.toJSONString(r));
        model.addAttribute("orders", r);

        return "orderList";
    }
}
