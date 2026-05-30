package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "支付信息")
public class PaymentInfoVO {

    @Schema(description = "支付流水号")
    private String paySn;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "支付时间")
    private String payTime;
}
