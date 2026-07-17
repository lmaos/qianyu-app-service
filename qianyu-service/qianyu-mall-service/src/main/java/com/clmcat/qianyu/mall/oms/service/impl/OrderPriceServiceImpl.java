package com.clmcat.qianyu.mall.oms.service.impl;

import com.clmcat.qianyu.mall.coupon.mapper.SmsCouponMapper;
import com.clmcat.qianyu.mall.coupon.mapper.SmsUserCouponMapper;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsUserCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.status.CouponStatus;
import com.clmcat.qianyu.mall.oms.model.vo.PriceResult;
import com.clmcat.qianyu.mall.oms.service.OrderPriceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 价格计算引擎实现。
 * 在传入的 totalAmount 基础上叠加 CouponCalculator 算抵扣；Freight/Promotion 为 Phase2/3 占位。
 */
@Slf4j
@Service
public class OrderPriceServiceImpl implements OrderPriceService {

    @Resource
    private SmsUserCouponMapper userCouponMapper;

    @Resource
    private SmsCouponMapper couponMapper;

    @Override
    public PriceResult calculatePrice(long userId, BigDecimal totalAmount, Long userCouponId) {
        BigDecimal freightAmount = BigDecimal.ZERO; // Phase2 占位

        // Coupon 计算（只读，不锁；锁在 createOrder CAS）
        BigDecimal couponAmount = BigDecimal.ZERO;
        Long appliedCouponId = null;
        if (userCouponId != null && userCouponId > 0) {
            SmsUserCoupon uc = userCouponMapper.selectOneById(userCouponId);
            if (uc != null && uc.getUserId() != null && uc.getUserId().longValue() == userId
                    && uc.getStatus() != null && uc.getStatus() == CouponStatus.UC_UNUSED) {
                SmsCoupon c = couponMapper.selectOneById(uc.getCouponId());
                if (c != null && c.getStatus() != null && c.getStatus() == CouponStatus.COUPON_ENABLED) {
                    couponAmount = calcCouponDiscount(c, totalAmount);
                    appliedCouponId = userCouponId;
                    log.info("[price] 用户{} 券{} 抵扣={}", userId, userCouponId, couponAmount);
                }
            } else {
                log.warn("[price] 券{} 不可用（归属/状态/过期），忽略", userCouponId);
            }
        }

        BigDecimal payAmount = totalAmount.add(freightAmount).subtract(couponAmount);
        if (payAmount.compareTo(new BigDecimal("0.01")) < 0) {
            payAmount = new BigDecimal("0.01");
        }

        return PriceResult.builder()
                .totalAmount(totalAmount)
                .freightAmount(freightAmount)
                .couponAmount(couponAmount)
                .payAmount(payAmount)
                .appliedUserCouponId(appliedCouponId)
                .build();
    }

    /**
     * 券抵扣：type1满减→min(discountAmount, total)；type2折扣→total×(1-rate/10)；type3免运费→0
     */
    private BigDecimal calcCouponDiscount(SmsCoupon c, BigDecimal totalAmount) {
        if (c.getType() == null || c.getThreshold() == null) return BigDecimal.ZERO;
        if (totalAmount.compareTo(c.getThreshold()) < 0) return BigDecimal.ZERO;
        return switch (c.getType()) {
            case 1 -> {
                BigDecimal amt = c.getDiscountAmount() != null ? c.getDiscountAmount() : BigDecimal.ZERO;
                yield amt.min(totalAmount);
            }
            case 2 -> {
                BigDecimal rate = c.getDiscountRate() != null ? c.getDiscountRate() : BigDecimal.TEN;
                yield totalAmount.multiply(
                        BigDecimal.ONE.subtract(rate.divide(BigDecimal.TEN, 4, RoundingMode.HALF_UP))
                ).setScale(2, RoundingMode.HALF_UP);
            }
            case 3 -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }
}
