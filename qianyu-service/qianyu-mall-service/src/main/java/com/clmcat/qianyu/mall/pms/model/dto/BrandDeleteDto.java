package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "删除品牌请求")
public class BrandDeleteDto {

    @Schema(description = "品牌 ID")
    private Long brandId;
}
