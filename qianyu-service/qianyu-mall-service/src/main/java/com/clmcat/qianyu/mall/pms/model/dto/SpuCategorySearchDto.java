package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分类搜索商品请求")
public class SpuCategorySearchDto {

    @Schema(description = "分类 ID（L2 或 L3）")
    private Long categoryId;

    @Schema(description = "排序方式: recommend=综合(默认) / sales=销量 / price=价格")
    private String sortMode;

    @Schema(description = "价格排序方向: asc=升序 / desc=降序（sortMode=price 时生效）")
    private String priceDirection;

    @Schema(description = "页码（默认 1）")
    private Integer pageNum;

    @Schema(description = "每页条数（默认 10）")
    private Integer pageSize;
}
