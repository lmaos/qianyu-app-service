package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "店铺首页聚合查询")
public class StoreHomeQueryDTO {

    @Schema(description = "商家 ID（必填）")
    private Long merchantId;

    @Schema(description = "热销商品数量（默认 6）")
    private Integer hotLimit;

    @Schema(description = "新品数量（默认 6）")
    private Integer newLimit;
}
