package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "购物车项")
public class CartItemVO {

    @Schema(description = "购物车项 ID")
    private Long id;

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "SKU 图片 URL")
    private String skuImage;

    @Schema(description = "规格描述")
    private String skuSpecs;

    @Schema(description = "单价（元）")
    private String price;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "当前库存")
    private Integer stock;

    @Schema(description = "是否选中")
    private Boolean checked;

    @Schema(description = "商家 ID")
    private Long merchantId;

    @Schema(description = "商家名称")
    private String merchantName;
}
