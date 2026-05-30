package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "分类树节点")
public class CategoryTreeVo {

    @Schema(description = "分类 ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类图标 URL")
    private String icon;

    @Schema(description = "分类图片资源 ID")
    private Long imgId;

    @Schema(description = "排序值，越小越靠前")
    private Integer sort;

    @Schema(description = "子分类列表")
    private List<CategoryTreeVo> children;
}
