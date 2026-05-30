package com.clmcat.qianyu.mall.inv.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "库存释放项")
public class StockReleaseItem {

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "释放数量")
    private Integer quantity;
}
