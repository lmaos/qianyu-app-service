package com.clmcat.qianyu.mall.log.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "物流列表分页查询请求")
public class LogisticsListQueryDTO {

    @Schema(description = "物流单号（模糊搜索）")
    private String shippingNo;

    @Schema(description = "页码，默认 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，默认 10")
    private Integer pageSize = 10;
}
