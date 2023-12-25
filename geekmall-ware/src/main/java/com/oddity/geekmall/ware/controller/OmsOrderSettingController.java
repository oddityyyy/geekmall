package com.oddity.geekmall.ware.controller;

import java.util.Arrays;
import java.util.Map;

import com.oddity.geekmall.ware.entity.OmsOrderSettingEntity;
import com.oddity.geekmall.ware.service.OmsOrderSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.R;



/**
 * 订单配置信息
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:28:36
 */
@RestController
@RequestMapping("ware/omsordersetting")
public class OmsOrderSettingController {
    @Autowired
    private OmsOrderSettingService omsOrderSettingService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:omsordersetting:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = omsOrderSettingService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:omsordersetting:info")
    public R info(@PathVariable("id") Long id){
		OmsOrderSettingEntity omsOrderSetting = omsOrderSettingService.getById(id);

        return R.ok().put("omsOrderSetting", omsOrderSetting);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:omsordersetting:save")
    public R save(@RequestBody OmsOrderSettingEntity omsOrderSetting){
		omsOrderSettingService.save(omsOrderSetting);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:omsordersetting:update")
    public R update(@RequestBody OmsOrderSettingEntity omsOrderSetting){
		omsOrderSettingService.updateById(omsOrderSetting);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:omsordersetting:delete")
    public R delete(@RequestBody Long[] ids){
		omsOrderSettingService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
