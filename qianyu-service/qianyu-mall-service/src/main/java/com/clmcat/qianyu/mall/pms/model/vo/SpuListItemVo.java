package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "分类搜索商品列表项（最小字段集）")
public class SpuListItemVo {

    @Schema(description = "SPU ID")
    private Long id;

    @Schema(description = "商品名称")
    private String title;

    @Schema(description = "主图 URL")
    private String mainImage;

    @Schema(description = "最低价格（元）")
    private String price;

    @Schema(description = "原价（元）")
    private String originalPrice;

    @Schema(description = "店铺名称")
    private String shopName;
}
