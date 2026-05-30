package com.clmcat.qianyu.mall.inv.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "锁定失败项")
public class StockLockFailItemVO {

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "当前可用库存")
    private Integer available;

    @Schema(description = "请求锁定数量")
    private Integer requested;
}
