package com.clmcat.qianyu.mall.ads.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "地区节点")
public class RegionNodeVO {

    @Schema(description = "地区 ID")
    private Long id;

    @Schema(description = "地区名称")
    private String name;

    @Schema(description = "父级 ID")
    private Long parentId;

    @Schema(description = "层级：1-省, 2-市, 3-区")
    private Integer level;
}
