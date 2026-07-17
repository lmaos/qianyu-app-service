package com.clmcat.qianyu.mall.coupon.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "下单可用券查询")
public class CouponUsableDTO {
    @Schema(description = "订单商品总金额（用于门槛/折扣试算）")
    private BigDecimal orderAmount;
}
