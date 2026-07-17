package com.clmcat.qianyu.mall.oms.service.impl;

import com.clmcat.qianyu.mall.coupon.mapper.SmsCouponMapper;
import com.clmcat.qianyu.mall.coupon.mapper.SmsUserCouponMapper;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsUserCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.status.CouponStatus;
import com.clmcat.qianyu.mall.oms.model.vo.FreightContext;
import com.clmcat.qianyu.mall.oms.model.vo.PriceResult;
import com.clmcat.qianyu.mall.oms.service.OrderPriceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class OrderPriceServiceImpl implements OrderPriceService {

    @Resource
    private SmsUserCouponMapper userCouponMapper;
    @Resource
    private SmsCouponMapper couponMapper;
    @Resource
    private FreightCalculator freightCalculator;
    @Resource
    private PromotionCalculator promotionCalculator;

    @Override
    public PriceResult calculatePrice(long userId, BigDecimal totalAmount, Long userCouponId) {
        return calculatePrice(userId, totalAmount, userCouponId, null);
    }

    @Override
    public PriceResult calculatePrice(long userId, BigDecimal totalAmount, Long userCouponId, FreightContext freightCtx) {
        // 1. Freight（Phase2：实际计算；freightCtx=null → 0）
        BigDecimal freightAmount = freightCtx != null
                ? freightCalculator.calc(freightCtx, totalAmount)
                : BigDecimal.ZERO;

        // 2. Coupon（先算非免运费券的抵扣）
        BigDecimal couponAmount = BigDecimal.ZERO;
        Long appliedCouponId = null;
        SmsCoupon appliedCoupon = null;
        if (userCouponId != null && userCouponId > 0) {
            SmsUserCoupon uc = userCouponMapper.selectOneById(userCouponId);
            if (uc != null && uc.getUserId() != null && uc.getUserId().longValue() == userId
                    && uc.getStatus() != null && uc.getStatus() == CouponStatus.UC_UNUSED) {
                SmsCoupon c = couponMapper.selectOneById(uc.getCouponId());
                if (c != null && c.getStatus() != null && c.getStatus() == CouponStatus.COUPON_ENABLED) {
                    appliedCoupon = c;
                    appliedCouponId = userCouponId;
                }
            }
        }

        // 免运费券（type=3）：抵扣运费
        if (appliedCoupon != null && appliedCoupon.getType() != null && appliedCoupon.getType() == 3) {
            couponAmount = freightAmount; // 免运费券抵扣 = 运费金额
            log.info("[price] 免运费券{} 抵扣运费={}", appliedCouponId, couponAmount);
        } else if (appliedCoupon != null) {
            couponAmount = calcCouponDiscount(appliedCoupon, totalAmount);
            log.info("[price] 券{} 抵扣={}", appliedCouponId, couponAmount);
        }

        // 3. Promotion（满减：Phase3）
        BigDecimal promotionDiscount = BigDecimal.ZERO;
        if (freightCtx != null && freightCtx.getMerchantId() != null) {
            promotionDiscount = promotionCalculator.calc(freightCtx.getMerchantId(), totalAmount);
            if (promotionDiscount.compareTo(BigDecimal.ZERO) > 0) {
                log.info("[price] 满减抵扣={} merchantId={}", promotionDiscount, freightCtx.getMerchantId());
            }
        }

        // 4. payAmount = total + freight - coupon - promotion（下限 0.01）
        BigDecimal payAmount = totalAmount.add(freightAmount).subtract(couponAmount).subtract(promotionDiscount);
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
            default -> BigDecimal.ZERO; // type=3 由外层处理
        };
    }
}
