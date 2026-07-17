package com.clmcat.qianyu.mall.coupon.model.vo;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
public class MatchedCouponVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userCouponId;
    private Long couponId;
    private String name;
    private Integer type;
    private String typeText;
    private String threshold;
    private String discountAmount;
    private String discountText;
    private Integer scopeType;
    private String scopeValue;
    private boolean usable;
    private String reason;
}
