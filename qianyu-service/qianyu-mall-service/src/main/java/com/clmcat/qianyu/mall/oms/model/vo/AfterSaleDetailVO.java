package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "售后详情")
public class AfterSaleDetailVO {

    @Schema(description = "售后单 ID")
    private Long id;

    @Schema(description = "售后单号")
    private String aftersaleSn;

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "关联订单明细 ID")
    private Long orderItemId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "售后类型")
    private Integer type;

    @Schema(description = "类型中文")
    private String typeText;

    @Schema(description = "售后状态")
    private Integer status;

    @Schema(description = "状态中文")
    private String statusText;

    @Schema(description = "售后原因")
    private String reason;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "凭证图片列表")
    private List<String> images;

    @Schema(description = "商家拒绝原因")
    private String rejectReason;

    @Schema(description = "退款金额（元）")
    private String refundAmount;

    @Schema(description = "退货物流单号")
    private String returnShippingNo;

    @Schema(description = "退货物流公司")
    private String returnShippingCompany;

    @Schema(description = "商家寄回物流单号")
    private String sendBackShippingNo;

    @Schema(description = "商家寄回物流公司")
    private String sendBackShippingCompany;

    @Schema(description = "退款完成时间")
    private String refundTime;

    @Schema(description = "涉及商品列表")
    private List<OrderItemDetailVO> items;

    @Schema(description = "处理日志列表")
    private List<AftersaleLogVO> logList;
}
