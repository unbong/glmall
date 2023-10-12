package com.atguigu.common.exception;

import lombok.Getter;
import lombok.Setter;

public class NoStockException extends RuntimeException{
    @Setter @Getter
    public Long skuId;
    public NoStockException(Long skuId){
        super("商品ID：" + skuId + "库存不足");
    }

    public NoStockException(String message){
        super(message);
    }

}
