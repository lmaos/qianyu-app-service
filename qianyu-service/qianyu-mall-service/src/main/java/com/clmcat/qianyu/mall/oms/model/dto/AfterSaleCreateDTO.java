package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "售后申请请求")
public class AfterSaleCreateDTO {

    @Schema(description = "订单 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(description = "订单商品项 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderItemId;

    @Schema(description = "售后类型：1-仅退款, 2-退货退款, 3-换货, 4-维修", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "售后原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "凭证图片列表")
    private List<String> images;
}
