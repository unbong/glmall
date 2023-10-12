package com.atguigu.gulimall.order.vo;


import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {

    @Getter @Setter
    // 收获地址列表
    private List<MemberAddressVo> memberAddressVos;

    /** 所有选中的购物项 **/
    @Getter @Setter
    private List<OrderItemVo> orderItemVos;

    /** 优惠券（会员积分） **/
    @Getter @Setter
    private Integer integretion;

    /** 防止重复提交的令牌 **/
    @Getter @Setter
    private String orderToken;



    @Getter @Setter
    Map<Long,Boolean> stocks;

    // 订单总额
    public BigDecimal getTotal()
    {
        BigDecimal totalNum = BigDecimal.ZERO;

        if (orderItemVos == null)
        {
            return totalNum;
        }
        for (OrderItemVo orderItemVo : orderItemVos) {

            totalNum = totalNum.add( orderItemVo.getTotalPrice());
        }

        return totalNum;
    }




    public Integer getCount() {
        Integer count = 0;
        if (orderItemVos != null && orderItemVos.size() > 0) {
            for (OrderItemVo item : orderItemVos) {
                count += item.getCount();
            }
        }
        return count;
    }

    /** 应付价格 **/
    //BigDecimal payPrice;
    public BigDecimal getPayPrice() {
        return getTotal();
    }

}
