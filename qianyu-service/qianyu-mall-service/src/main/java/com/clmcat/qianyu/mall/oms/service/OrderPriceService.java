package com.clmcat.qianyu.mall.oms.service;

import com.clmcat.qianyu.mall.oms.model.vo.PriceResult;

import java.math.BigDecimal;

/**
 * 价格计算引擎（算价入口）。
 * Calculator 链：Total → Freight(Phase2) → Coupon → Promotion(Phase3)。
 */
public interface OrderPriceService {

    /**
     * 在已算出的商品总额基础上，叠加券抵扣 → 返回完整价格结果（只读试算，不锁券）。
     *
     * @param userId       用户 ID（校验券归属）
     * @param totalAmount  已算出的商品总额（createOrder 内 SKU 循环产出）
     * @param userCouponId 用户券 ID（nullable=不用券）
     * @return {totalAmount, couponAmount, payAmount, appliedUserCouponId}
     */
    PriceResult calculatePrice(long userId, BigDecimal totalAmount, Long userCouponId);
}
