package com.clmcat.qianyu.mall.coupon.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "下单可用优惠券（含试算抵扣）")
public class UsableCouponVO {
    private Long userCouponId;
    private Long couponId;
    private String name;
    private Integer type;
    private String typeText;
    private String threshold;
    private String discountText;
    private String discountAmount; // 试算抵扣金额（元）
    private boolean usable;        // 是否可用（门槛/范围是否达标）
    private String reason;         // 不可用原因
}
