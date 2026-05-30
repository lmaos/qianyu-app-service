package com.clmcat.qianyu.mall.pay.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "退款结果响应")
public class RefundResultVO {

    @Schema(description = "退款流水号")
    private String refundSn;

    @Schema(description = "退款状态：PROCESSING/SUCCESS/FAILED")
    private String status;
}
