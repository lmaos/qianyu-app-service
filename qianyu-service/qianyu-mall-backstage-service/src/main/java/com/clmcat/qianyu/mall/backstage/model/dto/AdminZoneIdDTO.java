package com.clmcat.qianyu.mall.backstage.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "楼层/楼层商品 目标 ID（多用途：zoneDelete/zoneProductList/zoneProductRemove 取 id；setStatus 取 id+status）")
public class AdminZoneIdDTO {
    @Schema(description = "目标 ID（楼层 ID 或楼层商品 ID）")
    private Long id;
    @Schema(description = "状态 0显示1隐藏（setStatus 用）")
    private Integer status;
}
