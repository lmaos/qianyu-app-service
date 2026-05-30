package com.clmcat.qianyu.mall.inv.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "库存锁定请求")
public class StockLockDTO {

    @Schema(description = "关联订单编号")
    private String orderSn;

    @Schema(description = "锁定项列表")
    private List<StockLockItem> items;
}
