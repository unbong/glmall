package com.atguigu.gulimall.product.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catalog2Vo {
    // 1及目录ID
    // 2及目录
    // 3 及目录

    private String catalog1Id;
    private List<Catalog3Vo> catalog3List;
    private   String id;
    private  String name;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catalog3Vo
    {
        private String catalog2Id;
        private  String id;
        private  String name;

    }

}
