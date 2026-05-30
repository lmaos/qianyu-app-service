package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "订单商品摘要")
public class OrderItemSimpleVO {

    @Schema(description = "SKU 图片")
    private String skuImage;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "规格描述")
    private String skuSpecs;

    @Schema(description = "单价（元）")
    private String price;

    @Schema(description = "数量")
    private Integer quantity;
}
