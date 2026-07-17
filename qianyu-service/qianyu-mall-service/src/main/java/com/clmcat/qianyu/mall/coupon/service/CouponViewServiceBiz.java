package com.clmcat.qianyu.mall.coupon.service;

import com.clmcat.qianyu.mall.coupon.model.vo.CouponCardVO;
import com.clmcat.qianyu.mall.coupon.model.vo.UsableCouponVO;
import com.clmcat.qianyu.mall.coupon.model.vo.UserCouponVO;
import com.mybatisflex.core.paginate.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CouponViewServiceBiz {

    /** 可领券列表 */
    List<CouponCardVO> claimableCoupons(long userId, Long merchantId);

    /** 领取优惠券（CAS 扣库存 + 限领校验） */
    Long claim(long userId, Long couponId);

    /** 我的优惠券（status: 1未用 2已用 3过期, 0=全部） */
    Page<UserCouponVO> myCoupons(long userId, int status, int pageNum, int pageSize);

    /** 下单可用券（试算抵扣） */
    List<UsableCouponVO> usableForOrder(long userId, BigDecimal orderAmount);
}
