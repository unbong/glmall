package com.atguigu.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

public class CartVo {

    private List<CartItemVo> items;



    private Integer countNum;

    private Integer countType;

    private BigDecimal totalAmount;

    private BigDecimal reduce;

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {

        Integer count =0;
        if (items != null&& items.size() > 0)
        {
            for (CartItemVo item : items)
            {
                count += item.getCount();
            }
        }


        return count;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public Integer getCountType() {
        return countType;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }

    public BigDecimal getTotalAmount() {

        BigDecimal total = new BigDecimal(0);
        if (items != null && items.size() >0)
        {
            for(CartItemVo item : items)
            {
                total = total.add(item.getTotalPrice());

            }

        }

        total = total.subtract(reduce);

        return total;
    }

    public void setTotalAmount(BigDecimal totalAmount) {


        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
