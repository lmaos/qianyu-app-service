package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "购物车加入商品请求")
public class CartAddDTO {

    @Schema(description = "SPU ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long spuId;

    @Schema(description = "SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long skuId;

    @Schema(description = "数量，大于0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
