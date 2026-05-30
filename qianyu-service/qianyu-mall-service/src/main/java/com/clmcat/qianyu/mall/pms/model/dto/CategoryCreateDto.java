package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建分类请求")
public class CategoryCreateDto {

    @Schema(description = "分类图片地址")
    private String imgId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父分类 ID，不传为一级分类")
    private Long parentId;

    @Schema(description = "分类图标 URL")
    private String icon;

    @Schema(description = "排序值")
    private Integer sort;
}
