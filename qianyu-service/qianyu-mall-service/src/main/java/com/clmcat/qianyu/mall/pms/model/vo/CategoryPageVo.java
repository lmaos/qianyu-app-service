package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "全部分类页数据")
public class CategoryPageVo {

    @Schema(description = "一级分类列表")
    private List<CategoryFirstVo> firstCategoryList;

    @Schema(description = "初始选中的一级分类 ID")
    private Long initialFirstCategoryId;
}
