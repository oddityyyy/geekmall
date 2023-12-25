package com.oddity.geekmall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.oddity.geekmall.product.entity.BrandEntity;
import com.oddity.geekmall.product.vo.BrandVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.oddity.geekmall.product.entity.CategoryBrandRelationEntity;
import com.oddity.geekmall.product.service.CategoryBrandRelationService;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 20:36:07
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @GetMapping("/brands/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R relationBrandslist(@RequestParam(value = "catId", required = true) Long catId){
        List<BrandEntity> brands = categoryBrandRelationService.getBrandsByCatId(catId);
        List<BrandVo> data = brands.stream().map((brand) -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(brand.getBrandId());
            brandVo.setBrandName(brand.getName());
            return brandVo;
        }).collect(Collectors.toList());

        return R.ok().put("data", data);
    }

    /**
     * 获取当前品牌关联的所有分类列表
     */
    @GetMapping("/catelog/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R cateloglist(@RequestParam("brandId") Long brandId){
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));

        return R.ok().put("data", data);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
