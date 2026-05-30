package com.clmcat.qianyu.mall.inv.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "库存调整请求")
public class StockAdjustDTO {

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "调整类型：1-增加, 2-减少")
    private Integer adjustType;

    @Schema(description = "调整数量，大于 0")
    private Integer quantity;

    @Schema(description = "调整原因")
    private String reason;
}
