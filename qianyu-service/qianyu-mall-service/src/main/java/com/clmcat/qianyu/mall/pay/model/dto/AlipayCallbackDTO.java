package com.clmcat.qianyu.mall.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "支付宝回调通知")
public class AlipayCallbackDTO {

    @Schema(description = "商户订单号（系统 paySn）")
    private String outTradeNo;

    @Schema(description = "支付宝交易号")
    private String tradeNo;

    @Schema(description = "交易状态")
    private String tradeStatus;

    @Schema(description = "订单金额（元）")
    private String totalAmount;

    @Schema(description = "买家支付宝用户号")
    private String buyerId;

    @Schema(description = "原始回调参数 Map")
    private Map<String, String> rawParams;
}
