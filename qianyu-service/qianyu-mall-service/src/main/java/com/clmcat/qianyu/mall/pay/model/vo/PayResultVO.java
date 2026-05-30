package com.clmcat.qianyu.mall.pay.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "支付结果查询响应")
public class PayResultVO {

    @Schema(description = "支付流水号")
    private String paySn;

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "支付状态：PENDING/SUCCESS/FAILED/CLOSED")
    private String status;

    @Schema(description = "支付渠道：wechat/alipay/balance")
    private String payChannel;

    @Schema(description = "实付金额（元）")
    private String payAmount;

    @Schema(description = "支付成功时间")
    private String payTime;
}
