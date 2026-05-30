package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "品牌列表请求")
public class BrandListDto {

    @Schema(description = "按分类 ID 筛选")
    private List<Long> categoryIds;

    @Schema(description = "品牌名关键词搜索")
    private String keyword;
}
