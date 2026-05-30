package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "三级分类项（分类页网格）")
public class CategoryThirdVo {

    @Schema(description = "三级分类 ID")
    private Long id;

    @Schema(description = "三级分类名称")
    private String name;

    @Schema(description = "分类图片 URL")
    private String imageUrl;
}
