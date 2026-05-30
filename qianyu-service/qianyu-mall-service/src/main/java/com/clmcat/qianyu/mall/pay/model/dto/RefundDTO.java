package com.clmcat.qianyu.mall.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "退款请求")
public class RefundDTO {

    @Schema(description = "原支付流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paySn;

    @Schema(description = "退款金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refundAmount;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "外部退款单号（业务系统生成）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String outRefundNo;
}
