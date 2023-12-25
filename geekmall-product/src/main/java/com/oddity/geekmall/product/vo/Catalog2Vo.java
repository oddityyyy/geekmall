package com.oddity.geekmall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author oddity
 * @create 2023-08-09 0:08
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catalog2Vo {
    private String catalog1Id;
    private List<Catalog3Vo> catalog3List;
    private String id;
    private String name;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catalog3Vo {
        private String catalog2Id;
        private String id;
        private String name;
    }
}
