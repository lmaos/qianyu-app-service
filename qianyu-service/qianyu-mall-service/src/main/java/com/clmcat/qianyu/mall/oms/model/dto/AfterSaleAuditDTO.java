package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "售后审批请求")
public class AfterSaleAuditDTO {

    @Schema(description = "售后单 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long aftersaleId;

    @Schema(description = "true-同意, false-拒绝", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean approved;

    @Schema(description = "拒绝原因（拒绝时必填）")
    private String rejectReason;

    @Schema(description = "实际退款金额（元），不传则全额退款")
    private String refundAmount;
}
