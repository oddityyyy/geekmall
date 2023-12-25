package com.oddity.geekmall.seckill.controller;

import com.oddity.common.utils.R;
import com.oddity.geekmall.seckill.service.SeckillService;
import com.oddity.geekmall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author oddity
 * @create 2023-10-29 16:42
 */

@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SecKillSkuRedisTo> tos = seckillService.getCurrentSeckillSkus();
        return R.ok().put("data", tos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().put("data", to);
    }

    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId, @RequestParam("key") String key, @RequestParam("num") Integer num, Model model){

        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);

        return "success";
    }
}
