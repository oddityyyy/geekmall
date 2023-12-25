package com.oddity.geekmall.product.feign.fallback;

import com.oddity.common.exception.BizCodeEnume;
import com.oddity.common.utils.R;
import com.oddity.geekmall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author oddity
 * @create 2023-10-31 23:47
 */

@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {

    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("熔断方法调用...getSkuSeckillInfo");
        return R.error(BizCodeEnume.TOO_MANY_REQUEST.getCode(), BizCodeEnume.TOO_MANY_REQUEST.getMsg());
    }
}
