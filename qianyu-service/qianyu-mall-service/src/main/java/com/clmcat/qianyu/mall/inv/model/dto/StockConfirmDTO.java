package com.clmcat.qianyu.mall.inv.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "库存确认请求")
public class StockConfirmDTO {

    @Schema(description = "关联订单 ID")
    private Long orderId;
}
