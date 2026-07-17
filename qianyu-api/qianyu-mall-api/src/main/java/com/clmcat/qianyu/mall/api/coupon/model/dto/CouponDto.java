package com.clmcat.qianyu.mall.api.coupon.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券 DTO（Dubbo 传输 + Controller 出参）。
 */
@Data
public class CouponDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long merchantId;       // 0=平台券 >0=商家券
    private String name;
    private Integer type;          // 1满减 2折扣 3免运费
    private Integer scopeType;     // 1全平台 2指定商家 3品类 4商品
    private String scopeValue;     // JSON 数组（scope_type>1 时有值）
    private BigDecimal threshold;
    private BigDecimal discountAmount;
    private BigDecimal discountRate; // 8.50=85折
    private Integer totalCount;
    private Integer remainCount;
    private Integer perLimit;
    private Long startTime;
    private Long endTime;
    private Integer status;        // 0禁 1启 2过期
    private Long createTime;
}
