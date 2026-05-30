package com.clmcat.qianyu.mall.inv.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "库存日志项")
public class StockLogItemVO {

    @Schema(description = "日志 ID")
    private Long id;

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "规格描述")
    private String skuSpecs;

    @Schema(description = "变动类型")
    private Integer type;

    @Schema(description = "类型中文")
    private String typeText;

    @Schema(description = "变动数量（正数为增加，负数为减少）")
    private Integer quantity;

    @Schema(description = "变动前库存")
    private Integer beforeStock;

    @Schema(description = "变动后库存")
    private Integer afterStock;

    @Schema(description = "关联订单号")
    private String orderSn;

    @Schema(description = "原因/备注")
    private String reason;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "变动时间")
    private String createTime;
}
