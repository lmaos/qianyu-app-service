package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "创建订单响应")
public class OrderCreateVO {

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "订单总金额（元）")
    private String totalAmount;

    @Schema(description = "运费金额（元）")
    private String freightAmount;

    @Schema(description = "促销优惠金额（元）")
    private String discountAmount;

    @Schema(description = "优惠券抵扣金额（元）")
    private String couponAmount;

    @Schema(description = "实付金额（元）")
    private String payAmount;

    @Schema(description = "订单总商品件数")
    private Integer totalQuantity;
}
