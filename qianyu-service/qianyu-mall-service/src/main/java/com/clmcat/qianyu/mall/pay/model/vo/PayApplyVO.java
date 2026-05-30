package com.clmcat.qianyu.mall.pay.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "发起支付响应")
public class PayApplyVO {

    @Schema(description = "支付流水号")
    private String paySn;

    @Schema(description = "支付参数（不同渠道结构不同）")
    private Object payParams;
}
