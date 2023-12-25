package com.oddity.geekmall.product.web;

import com.oddity.geekmall.product.entity.CategoryEntity;
import com.oddity.geekmall.product.service.CategoryService;
import com.oddity.geekmall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author oddity
 * @create 2023-08-08 19:26
 */

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping(value = {"/", "/index.html"})
    public String indexPage(Model model){

        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categoryEntities);

        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson(){
        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }
}
