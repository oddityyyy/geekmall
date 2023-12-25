package com.oddity.geekmall.product;

import com.oddity.geekmall.product.dao.AttrGroupDao;
import com.oddity.geekmall.product.dao.SkuSaleAttrValueDao;
import com.oddity.geekmall.product.entity.BrandEntity;
import com.oddity.geekmall.product.service.BrandService;
import com.oddity.geekmall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class GeekmallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test(){
//        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(100L, 225L);
//        System.out.println(attrGroupWithAttrsBySpuId);
        System.out.println(skuSaleAttrValueDao.getSaleAttrBySpuId(13L));
    }

    @Test
    public void testStringRedisTemplate(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello", "world_" + UUID.randomUUID().toString());
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Test
    public void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径: {}", Arrays.asList(catelogPath));
    }

    @Test
    void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("华为");
//        brandService.updateById(brandEntity);
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach((item) -> System.out.println(item));
    }

}
