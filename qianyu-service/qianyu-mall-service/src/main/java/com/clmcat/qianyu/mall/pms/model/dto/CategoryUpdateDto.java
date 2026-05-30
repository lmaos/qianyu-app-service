package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新分类请求")
public class CategoryUpdateDto {

    @Schema(description = "分类 ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父分类 ID")
    private Long parentId;

    @Schema(description = "图标 URL")
    private String icon;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
