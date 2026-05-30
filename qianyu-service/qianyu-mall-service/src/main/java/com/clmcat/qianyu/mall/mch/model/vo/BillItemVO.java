package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "账单明细项")
public class BillItemVO {

    @Schema(description = "账单 ID")
    private Long id;

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "账单类型：1=订单收入 2=退款支出 3=佣金调整")
    private Integer type;

    @Schema(description = "类型中文")
    private String typeText;

    @Schema(description = "订单金额（元）")
    private String orderAmount;

    @Schema(description = "退款金额（元）")
    private String refundAmount;

    @Schema(description = "平台佣金（元）")
    private String platformFee;

    @Schema(description = "平台佣金比例（%）")
    private BigDecimal platformRate;

    @Schema(description = "主播佣金（元）")
    private String anchorFee;

    @Schema(description = "商家实际入账（元）")
    private String merchantIncome;

    @Schema(description = "结算状态：0=未结算 1=已结算")
    private Integer status;

    @Schema(description = "创建时间")
    private String createTime;
}
