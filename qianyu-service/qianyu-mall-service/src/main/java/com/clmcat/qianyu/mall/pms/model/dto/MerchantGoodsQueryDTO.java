package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商家商品管理列表请求")
public class MerchantGoodsQueryDTO {

    @Schema(description = "筛选: 0=全部(默认), 1=在售, 2=待上架(草稿+下架), 3=库存预警(P2)")
    private Integer filter;

    @Schema(description = "页码，默认 1")
    private Integer pageNum;

    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;
}
