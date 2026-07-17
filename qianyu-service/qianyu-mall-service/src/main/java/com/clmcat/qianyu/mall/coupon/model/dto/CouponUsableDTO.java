package com.clmcat.qianyu.mall.coupon.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "下单可用券查询")
public class CouponUsableDTO {
    @Schema(description = "订单商品总金额（用于门槛/折扣试算）")
    private BigDecimal orderAmount;
    @Schema(description = "商家 ID（scope_type=2 匹配用）")
    private Long merchantId;
    @Schema(description = "商品项列表（scope 匹配用）")
    private List<CouponItem> items;

    @Data
    public static class CouponItem {
        private Long spuId;
        private Long categoryId;
    }
}
