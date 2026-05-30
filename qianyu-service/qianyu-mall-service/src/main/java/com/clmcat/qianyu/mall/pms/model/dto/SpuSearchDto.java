package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "SPU 搜索请求")
public class SpuSearchDto {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "分类 ID")
    private Long categoryId;

    @Schema(description = "品牌 ID")
    private Long brandId;

    @Schema(description = "排序字段：price / sales / createTime，默认 createTime")
    private String sortField;

    @Schema(description = "排序方向：asc / desc，默认 desc")
    private String sortOrder;

    @Schema(description = "最低价格（元）")
    private String minPrice;

    @Schema(description = "最高价格（元）")
    private String maxPrice;

    @Schema(description = "页码，默认 1")
    private Integer pageNum;

    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;
}
