package com.oddity.common.exception;

/**
 * @author oddity
 * @create 2023-10-22 15:29
 */
public class NoStockException extends RuntimeException {

    private Long skuId;

    public NoStockException(String message) {
        super(message);
    }

    public NoStockException(Long skuId) {
        super("商品id:" + skuId + "，没有足够库存");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
