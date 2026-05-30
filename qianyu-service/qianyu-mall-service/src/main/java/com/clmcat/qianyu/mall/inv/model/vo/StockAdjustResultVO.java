package com.clmcat.qianyu.mall.inv.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "库存调整结果")
public class StockAdjustResultVO {

    @Schema(description = "调整前库存")
    private Integer beforeStock;

    @Schema(description = "调整后库存")
    private Integer afterStock;
}
