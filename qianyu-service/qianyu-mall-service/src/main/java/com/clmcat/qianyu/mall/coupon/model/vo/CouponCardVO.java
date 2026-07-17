package com.clmcat.qianyu.mall.coupon.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "可领优惠券卡")
public class CouponCardVO {
    private Long id;
    private String name;
    private Integer type;
    private String typeText;
    private String threshold;
    private String discountText;
    private Integer remainCount;
    private Integer perLimit;
    private Long endTime;
    private Integer claimed; // 当前用户已领数量（0=未领）
}
