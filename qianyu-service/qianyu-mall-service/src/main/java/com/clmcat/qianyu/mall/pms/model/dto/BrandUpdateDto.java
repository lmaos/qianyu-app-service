package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新品牌请求")
public class BrandUpdateDto {

    @Schema(description = "品牌 ID")
    private Long brandId;

    @Schema(description = "品牌名称")
    private String name;

    @Schema(description = "Logo URL")
    private String logo;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
