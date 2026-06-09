package com.clmcat.qianyu.mall.inv.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "库存分页列表项")
public class StockPageItemVO {

    @Schema(description = "库存记录 ID")
    private Long id;

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "SKU 名称")
    private String skuName;

    @Schema(description = "所属 SPU 名称")
    private String spuName;

    @Schema(description = "SKU 图片")
    private String skuImage;

    @Schema(description = "可用库存")
    private Integer availableStock;

    @Schema(description = "锁定库存")
    private Integer lockedStock;

    @Schema(description = "安全库存")
    private Integer safetyStock;
}
