package com.clmcat.qianyu.mall.inv.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "库存锁定项")
public class StockLockItem {

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "锁定数量")
    private Integer quantity;
}
