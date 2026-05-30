package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "二级分类项（分类页标签栏 + 分组）")
public class CategorySecondVo {

    @Schema(description = "二级分类 ID")
    private Long id;

    @Schema(description = "二级分类名称")
    private String name;

    @Schema(description = "三级分类列表")
    private List<CategoryThirdVo> thirdCategoryList;
}
