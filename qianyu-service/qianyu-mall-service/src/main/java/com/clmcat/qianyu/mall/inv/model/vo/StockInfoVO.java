package com.clmcat.qianyu.mall.inv.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "库存信息")
public class StockInfoVO {

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "总库存")
    private Integer total;

    @Schema(description = "可用库存")
    private Integer available;

    @Schema(description = "锁定库存")
    private Integer locked;
}
