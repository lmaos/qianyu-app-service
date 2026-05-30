package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "店铺商品列表查询请求")
public class ShopProductQueryDTO {

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "分类 ID 筛选")
    private Long categoryId;

    @Schema(description = "商品名称搜索")
    private String keyword;

    @Schema(description = "排序字段：price / sales / createTime")
    private String sortField;

    @Schema(description = "排序方向：asc / desc")
    private String sortOrder;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;
}
