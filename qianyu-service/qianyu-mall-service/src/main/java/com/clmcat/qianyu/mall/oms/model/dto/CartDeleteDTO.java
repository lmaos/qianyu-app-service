package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "购物车删除请求")
public class CartDeleteDTO {

    @Schema(description = "待删除的购物车项 ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> cartItemIds;
}
