package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "购物车更新数量请求")
public class CartUpdateDTO {

    @Schema(description = "购物车项 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cartItemId;

    @Schema(description = "新数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
