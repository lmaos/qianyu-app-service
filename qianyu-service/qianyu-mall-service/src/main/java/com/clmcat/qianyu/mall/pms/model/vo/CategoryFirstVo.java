package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "一级分类项（分类页左侧导航）")
public class CategoryFirstVo {

    @Schema(description = "一级分类 ID")
    private Long id;

    @Schema(description = "一级分类名称")
    private String name;

    @Schema(description = "二级分类列表")
    private List<CategorySecondVo> secondCategoryList;
}
