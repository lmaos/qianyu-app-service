package com.clmcat.qianyu.mall.backstage.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "楼层加商品")
public class AdminZoneProductAddDTO {
    @Schema(description = "楼层 ID")
    private Long zoneId;
    @Schema(description = "商品 SPU ID")
    private Long spuId;
    @Schema(description = "排序")
    private Integer sort;
}
