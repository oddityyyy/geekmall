package com.oddity.geekmall.search.service;

import com.oddity.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author oddity
 * @create 2023-08-03 21:05
 */
public interface ProductSaveService {

    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
