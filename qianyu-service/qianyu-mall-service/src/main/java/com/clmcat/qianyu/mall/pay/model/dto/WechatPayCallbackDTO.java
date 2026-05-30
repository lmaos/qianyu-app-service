package com.clmcat.qianyu.mall.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "微信支付回调通知")
public class WechatPayCallbackDTO {

    @Schema(description = "商户订单号（系统 paySn）")
    private String outTradeNo;

    @Schema(description = "微信支付订单号")
    private String transactionId;

    @Schema(description = "订单总金额（分）")
    private Integer totalFee;

    @Schema(description = "交易类型")
    private String tradeType;

    @Schema(description = "交易状态")
    private String tradeState;

    @Schema(description = "付款银行")
    private String bankType;

    @Schema(description = "支付完成时间")
    private String timeEnd;

    @Schema(description = "原始报文数据")
    private String rawData;
}
