package com.clmcat.qianyu.mall.log.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建物流单请求")
public class LogisticsCreateDTO {

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "订单明细 ID（拆物流时指定具体商品）")
    private Long orderItemId;

    @Schema(description = "物流公司名称")
    private String logisticsCompany;

    @Schema(description = "物流公司编码（如 SF=顺丰, YTO=圆通）")
    private String logisticsCode;

    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "寄件人姓名")
    private String senderName;

    @Schema(description = "寄件人电话")
    private String senderPhone;

    @Schema(description = "寄件人地址")
    private String senderAddress;
}
