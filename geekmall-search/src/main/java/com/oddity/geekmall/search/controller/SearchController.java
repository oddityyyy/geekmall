package com.oddity.geekmall.search.controller;

import com.oddity.geekmall.search.service.MallSearchService;
import com.oddity.geekmall.search.vo.SearchParam;
import com.oddity.geekmall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author oddity
 * @create 2023-09-20 23:39
 */

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @RequestMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){

        String queryString = request.getQueryString();
        param.set_queryString(queryString);

        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);

        return "list";
    }
}
