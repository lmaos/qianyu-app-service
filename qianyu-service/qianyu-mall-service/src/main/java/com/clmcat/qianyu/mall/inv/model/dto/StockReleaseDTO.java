package com.clmcat.qianyu.mall.inv.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "库存释放请求")
public class StockReleaseDTO {

    @Schema(description = "关联订单编号")
    private String orderSn;

    @Schema(description = "指定释放项（不传则释放该订单全部锁定）")
    private List<StockReleaseItem> items;
}
