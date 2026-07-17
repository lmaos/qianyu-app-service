package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格计算结果（PriceService 输出）。
 */
@Getter
@Builder
@Schema(description = "算价结果")
public class PriceResult {
    @Schema(description = "商品总金额")
    private BigDecimal totalAmount;
    @Schema(description = "运费（Phase2 占位=0）")
    private BigDecimal freightAmount;
    @Schema(description = "优惠券抵扣")
    private BigDecimal couponAmount;
    @Schema(description = "应付金额 = total + freight - coupon")
    private BigDecimal payAmount;
    @Schema(description = "使用的用户券 ID（核销后回填）")
    private Long appliedUserCouponId;
}
