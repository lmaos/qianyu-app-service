package com.clmcat.qianyu.mall.fav.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "收藏商品信息")
public class FavSpuInfoVO {

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "主图 URL")
    private String mainImage;

    @Schema(description = "价格（元）")
    private String price;

    @Schema(description = "原价（元）")
    private String originalPrice;

    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "是否在售")
    private Boolean onShelf;
}
