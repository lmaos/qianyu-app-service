package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "订单 ID 请求")
public class OrderIdDTO {

    @Schema(description = "订单 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;
}
