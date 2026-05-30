package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "售后列表项")
public class AfterSaleSimpleVO {

    @Schema(description = "售后单 ID")
    private Long id;

    @Schema(description = "售后单号")
    private String aftersaleSn;

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "售后类型")
    private Integer type;

    @Schema(description = "类型中文描述")
    private String typeText;

    @Schema(description = "售后状态")
    private Integer status;

    @Schema(description = "状态中文描述")
    private String statusText;

    @Schema(description = "退款金额（元）")
    private String refundAmount;

    @Schema(description = "申请时间")
    private String createTime;
}
