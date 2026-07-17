package com.clmcat.qianyu.mall.coupon.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "我的优惠券")
public class UserCouponVO {
    private Long id;           // sms_user_coupon.id
    private Long couponId;     // sms_coupon.id
    private String name;
    private Integer type;
    private String typeText;
    private String threshold;
    private String discountText;
    private Integer status;    // 1未用 2已用 3过期
    private String statusText;
    private Long expireTime;
    private Long createTime;
}
