package com.clmcat.qianyu.mall.api.coupon.model.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 优惠券分页查询（后台/商户管理用）。
 */
@Data
public class CouponPageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long merchantId;  // 0=平台, >0=商家, null=全部
    private Integer status;   // 0禁 1启 2过期, null=全部
    private String name;      // 模糊搜索名称
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
