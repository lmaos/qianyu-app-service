package com.clmcat.qianyu.mall.inv.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量库存查询请求")
public class StockBatchQueryDTO {

    @Schema(description = "SKU ID 列表，最多 100 个")
    private List<Long> skuIds;
}
