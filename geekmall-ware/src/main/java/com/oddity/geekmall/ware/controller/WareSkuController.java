package com.oddity.geekmall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.oddity.common.exception.BizCodeEnume;
import com.oddity.common.exception.NoStockException;
import com.oddity.geekmall.ware.vo.SkuHasStockVo;
import com.oddity.geekmall.ware.vo.WareSkuLockVo;
import com.oddity.geekmall.ware.entity.WareSkuEntity;
import com.oddity.geekmall.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.R;



/**
 * 商品库存
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:28:35
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {

    @Autowired
    private WareSkuService wareSkuService;

    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo){
        try {
            boolean stockResults = wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch (NoStockException e){
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(), BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    //查询skuIds是否有库存，一次批量查询，避免多次远程调用浪费资源
    @PostMapping("/hasstock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds){

        List<SkuHasStockVo> vos = wareSkuService.getSkusHasStock(skuIds);

        return R.ok().put("data", vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
