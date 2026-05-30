package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "删除分类请求")
public class CategoryDeleteDto {

    @Schema(description = "分类 ID")
    private Long categoryId;
}
