package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "品牌信息")
public class BrandVo {

    @Schema(description = "品牌 ID")
    private Long id;

    @Schema(description = "品牌名称")
    private String name;

    @Schema(description = "品牌 Logo URL")
    private String logo;

    @Schema(description = "品牌描述")
    private String description;
}
