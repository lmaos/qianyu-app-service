package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 买家填写退货物流请求（type=2 退货退款：商家同意后买家寄回）。
 */
@Data
@Schema(description = "买家退货物流")
public class AfterSaleReturnShipDTO {

    @Schema(description = "售后单 ID")
    private Long aftersaleId;

    @Schema(description = "退货物流单号")
    private String shippingNo;

    @Schema(description = "退货物流公司编码/名称")
    private String shippingCompany;
}
