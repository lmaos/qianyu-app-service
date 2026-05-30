package com.clmcat.qianyu.mall.log.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "物流查询请求")
public class LogisticsQueryDTO {

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "物流单 ID")
    private Long logisticsId;
}
