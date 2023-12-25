package com.oddity.geekmall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.oddity.common.valid.AddGroup;
import com.oddity.common.valid.UpdateGroup;
import com.oddity.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.oddity.geekmall.product.entity.BrandEntity;
import com.oddity.geekmall.product.service.BrandService;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.R;


/**
 * 品牌
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 20:36:07
 */

@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")
    //@RequiresPermissions("product:brand:info")
    public R info(@RequestParam List<Long> brandIds){
        List<BrandEntity> brands = brandService.getBrandsByIds(brandIds);

        return R.ok().put("brands", brands);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult result*/){
//        if (result.hasErrors()){
//            HashMap<String, String> map = new HashMap<>();
//            //1. 获取校验的错误结果
//            result.getFieldErrors().forEach((item) -> {
//                // item FieldError
//                String field = item.getField();
//                String message = item.getDefaultMessage();
//                map.put(field, message);
//            });
//            return R.error(400, "提交的数据不合法").put("data", map);
//        }else {
//            brandService.save(brand);
//        }
        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated({UpdateStatusGroup.class}) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
