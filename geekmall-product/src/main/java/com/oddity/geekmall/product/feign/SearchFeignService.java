package com.oddity.geekmall.product.feign;

import com.oddity.common.to.es.SkuEsModel;
import com.oddity.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author oddity
 * @create 2023-08-03 22:48
 */

@FeignClient("geekmall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
