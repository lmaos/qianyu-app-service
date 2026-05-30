package com.clmcat.qianyu.mall.pay.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "支付结果查询请求")
public class PayResultQueryDTO {

    @Schema(description = "支付流水号（与 orderId 二选一）")
    private String paySn;

    @Schema(description = "订单 ID（与 paySn 二选一）")
    private Long orderId;
}
