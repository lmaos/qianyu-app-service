package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "全部分类页请求")
public class CategoryPageDto {

    @Schema(description = "分类 ID（任意层级，不传则默认选中第一个 L1）")
    private Long categoryId;
}
