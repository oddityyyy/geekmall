package com.oddity.geekmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.oddity.geekmall.product.service.CategoryBrandRelationService;
import com.oddity.geekmall.product.vo.Catalog2Vo;
import com.oddity.geekmall.product.dao.CategoryDao;
import com.oddity.geekmall.product.entity.CategoryEntity;
import com.oddity.geekmall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oddity.common.utils.PageUtils;
import com.oddity.common.utils.Query;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1. 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2. 组装成父子的树形结构
        //2.1 找到所有一级分类
        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildrens(menu, entities));
                    return menu;
                })
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1. 检查当前删除的菜单是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    @CacheEvict(value = "category", allEntries = true) // 方法无返回值，只能支持失效模式
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

            //TODO 更新其他关联
        }
    }

    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys......");
//        Long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
//        System.out.println("消耗时间：" + (System.currentTimeMillis() - l));
        return categoryEntities;
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        System.out.println("查询了数据库......");
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        Map<String, List<Catalog2Vo>> collect = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<Catalog2Vo> catalog2Vos = null;
            List<CategoryEntity> categoryEntities2 = getParent_cid(selectList, v.getCatId());
            if (categoryEntities2 != null) {
                catalog2Vos = categoryEntities2.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> categoryEntities3 = getParent_cid(selectList, l2.getCatId());
                    if (categoryEntities3 != null) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = categoryEntities3.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        return collect;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            Map<String, List<Catalog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            //加锁成功
            //redisTemplate.expire("lock", 30, TimeUnit.SECONDS);

//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)){
//                redisTemplate.delete("lock");
//            }
            return dataFromDb;
        } else {
            System.out.println("获取分布式锁失败...等待重试");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    private Map<String, List<Catalog2Vo>> getDataFromDb() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return result;
        }
        System.out.println("查询了数据库......");

        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        Map<String, List<Catalog2Vo>> collect = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<Catalog2Vo> catalog2Vos = null;
            List<CategoryEntity> categoryEntities2 = getParent_cid(selectList, v.getCatId());
            if (categoryEntities2 != null) {
                catalog2Vos = categoryEntities2.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> categoryEntities3 = getParent_cid(selectList, l2.getCatId());
                    if (categoryEntities3 != null) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = categoryEntities3.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        String s = JSON.toJSONString(collect);
        redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
        return collect;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDb() {

        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        Map<String, List<Catalog2Vo>> collect = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<Catalog2Vo> catalog2Vos = null;
            List<CategoryEntity> categoryEntities2 = getParent_cid(selectList, v.getCatId());
            if (categoryEntities2 != null) {
                catalog2Vos = categoryEntities2.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> categoryEntities3 = getParent_cid(selectList, l2.getCatId());
                    if (categoryEntities3 != null) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = categoryEntities3.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        return collect;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> childrens = all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == root.getCatId())
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildrens(categoryEntity, all));
                    return categoryEntity;
                })
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return childrens;
    }
}