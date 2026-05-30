package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建品牌请求")
public class BrandCreateDto {

    @Schema(description = "品牌名称")
    private String name;

    @Schema(description = "品牌 Logo URL")
    private String logo;

    @Schema(description = "品牌描述")
    private String description;
}
