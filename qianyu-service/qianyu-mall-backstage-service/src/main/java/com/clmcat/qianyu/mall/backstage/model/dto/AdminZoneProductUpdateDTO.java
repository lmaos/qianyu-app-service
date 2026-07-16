package com.clmcat.qianyu.mall.backstage.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "楼层商品更新（排序/显隐）")
public class AdminZoneProductUpdateDTO {
    @Schema(description = "楼层商品 ID")
    private Long id;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "状态 0显示1隐藏")
    private Integer status;
}
