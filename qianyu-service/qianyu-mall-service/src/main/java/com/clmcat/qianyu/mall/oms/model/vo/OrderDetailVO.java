package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "订单详情")
public class OrderDetailVO {

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "订单状态")
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

    @Schema(description = "运费（元）")
    private String freightAmount;

    @Schema(description = "促销优惠金额（元）")
    private String discountAmount;

    @Schema(description = "优惠券抵扣金额（元）")
    private String couponAmount;

    @Schema(description = "优惠明细列表")
    private List<DiscountItemVO> discountDetail;

    @Schema(description = "买家留言")
    private String remark;

    @Schema(description = "商家内部备注")
    private String merchantRemark;

    @Schema(description = "商品列表")
    private List<OrderItemDetailVO> items;

    @Schema(description = "收货地址快照")
    private AddressSnapshotVO address;

    @Schema(description = "支付信息")
    private PaymentInfoVO payment;

    @Schema(description = "订单来源")
    private Integer source;

    @Schema(description = "下单时间")
    private String createTime;

    @Schema(description = "支付时间")
    private String payTime;

    @Schema(description = "发货时间")
    private String shipTime;

    @Schema(description = "确认收货时间")
    private String receiveTime;

    @Schema(description = "买家昵称（B端）")
    private String buyerNick;
}
