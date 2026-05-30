package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "入驻审核结果")
public class SettleResultVO {

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "申请编号")
    private String applySn;

    @Schema(description = "状态：1-待审核, 2-审核通过, 3-审核拒绝")
    private Integer status;

    @Schema(description = "状态中文")
    private String statusText;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "审核时间")
    private String auditTime;
}
