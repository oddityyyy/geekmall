package com.oddity.geekmall.order.controller;

import java.util.Arrays;
import java.util.Map;

import com.oddity.geekmall.order.entity.OrderItemEntity;
import com.oddity.geekmall.order.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.R;



/**
 * 订单项信息
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:18:44
 */
@RestController
@RequestMapping("order/orderitem")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:orderitem:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderItemService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:orderitem:info")
    public R info(@PathVariable("id") Long id){
		OrderItemEntity orderItem = orderItemService.getById(id);

        return R.ok().put("orderItem", orderItem);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:orderitem:save")
    public R save(@RequestBody OrderItemEntity orderItem){
		orderItemService.save(orderItem);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:orderitem:update")
    public R update(@RequestBody OrderItemEntity orderItem){
		orderItemService.updateById(orderItem);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("order:orderitem:delete")
    public R delete(@RequestBody Long[] ids){
		orderItemService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
