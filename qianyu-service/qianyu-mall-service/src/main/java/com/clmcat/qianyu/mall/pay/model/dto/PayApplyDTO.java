package com.clmcat.qianyu.mall.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发起支付请求")
public class PayApplyDTO {

    @Schema(description = "订单 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(description = "支付渠道：wechat / alipay / balance", requiredMode = Schema.RequiredMode.REQUIRED)
    private String payChannel;

    @Schema(description = "支付完成后前端跳转地址（H5支付需要）")
    private String returnUrl;
}
