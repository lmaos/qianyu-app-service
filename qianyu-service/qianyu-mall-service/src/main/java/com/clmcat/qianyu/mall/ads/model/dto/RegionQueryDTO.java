package com.clmcat.qianyu.mall.ads.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "地区树查询请求")
public class RegionQueryDTO {

    @Schema(description = "父级 ID，不传或传 0 查全部省份")
    private Long parentId;
}
