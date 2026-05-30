package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "订单列表项")
public class OrderSimpleVO {

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "订单状态：10/20/30/40/50/60")
    private Integer status;

    @Schema(description = "售后状态")
    private Integer afterSaleStatus;

    @Schema(description = "售后类型")
    private Integer afterSaleType;

    @Schema(description = "前端展示标签")
    private String displayStatus;

    @Schema(description = "订单总金额（元）")
    private String totalAmount;

    @Schema(description = "实付金额（元）")
    private String payAmount;

    @Schema(description = "订单总商品件数")
    private Integer totalQuantity;

    @Schema(description = "商品摘要列表")
    private List<OrderItemSimpleVO> items;

    @Schema(description = "下单时间")
    private String createTime;

    @Schema(description = "买家昵称（B端）")
    private String buyerNick;
}
