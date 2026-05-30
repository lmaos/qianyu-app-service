package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "订单商品项")
public class OrderItemDTO {

    @Schema(description = "SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long skuId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
