package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商家审核请求")
public class MerchantAuditDTO {

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "true-通过, false-拒绝")
    private Boolean approved;

    @Schema(description = "拒绝原因（拒绝时必填）")
    private String rejectReason;
}
