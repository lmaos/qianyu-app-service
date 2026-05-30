package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "SKU ID 请求")
public class SkuIdDto {

    @Schema(description = "SKU ID")
    private Long skuId;
}
