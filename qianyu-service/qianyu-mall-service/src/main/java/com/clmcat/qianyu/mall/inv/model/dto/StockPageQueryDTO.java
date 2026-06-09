package com.clmcat.qianyu.mall.inv.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "库存分页查询请求")
public class StockPageQueryDTO {

    @Schema(description = "关键词（SKU名称）")
    private String keyword;

    @Schema(description = "页码，默认 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，默认 10")
    private Integer pageSize = 10;
}
