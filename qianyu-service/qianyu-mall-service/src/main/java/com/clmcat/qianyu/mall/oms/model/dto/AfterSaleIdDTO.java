package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "售后单 ID 请求")
public class AfterSaleIdDTO {

    @Schema(description = "售后单 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long aftersaleId;
}
