package com.oddity.geekmall.search.service;

import com.oddity.geekmall.search.vo.SearchParam;
import com.oddity.geekmall.search.vo.SearchResult;

/**
 * @author oddity
 * @create 2023-09-27 22:36
 */
public interface MallSearchService {

    /**
     *
     * @param param 检索的所有参数
     * @return 返回检索的结果
     */
    SearchResult search(SearchParam param);
}
