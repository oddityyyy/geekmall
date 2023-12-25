package com.oddity.geekmall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author oddity
 * @create 2023-10-20 15:40
 */

@Controller
public class PageController {

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page){

        return page;
    }
}
