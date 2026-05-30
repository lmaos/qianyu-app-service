package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "订单明细详情")
public class OrderItemDetailVO {

    @Schema(description = "订单明细 ID")
    private Long id;

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "SKU 图片")
    private String skuImage;

    @Schema(description = "规格描述")
    private String skuSpecs;

    @Schema(description = "单价（元）")
    private String price;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计（元）")
    private String subtotal;
}
