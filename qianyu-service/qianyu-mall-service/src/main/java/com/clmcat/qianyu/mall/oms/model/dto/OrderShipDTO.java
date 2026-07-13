package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "订单发货请求")
public class OrderShipDTO {

    @Schema(description = "订单 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(description = "物流公司名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logisticsCompany;

    @Schema(description = "物流单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logisticsNo;

    @Schema(description = "物流公司编码（如 SF/YTO，发货必填，用于物流单创建校验）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logisticsCode;
}
