package com.clmcat.qianyu.mall.coupon.service;

import com.clmcat.qianyu.mall.coupon.mapper.SmsCouponMapper;
import com.clmcat.qianyu.mall.coupon.mapper.SmsUserCouponMapper;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsUserCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.status.CouponStatus;
import com.clmcat.qianyu.mall.coupon.model.vo.MatchedCouponVO;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
public class CouponMatchService {

    @Resource private SmsUserCouponMapper userCouponMapper;
    @Resource private SmsCouponMapper couponMapper;
    @Resource private CouponScopeValidator scopeValidator;

    public List<MatchedCouponVO> matchCoupons(long userId, List<Long> spuIds, List<Long> categoryIds,
                                               Long merchantId, BigDecimal totalAmount) {
        long now = System.currentTimeMillis();
        List<SmsUserCoupon> ucs = userCouponMapper.selectListByQuery(
                QueryWrapper.create().where("user_id = ?", userId).and("deleted = 0")
                        .and("status = 1").and("expire_time > ?", now));
        if (ucs == null || ucs.isEmpty()) return Collections.emptyList();

        Set<Long> couponIds = new HashSet<>();
        for (SmsUserCoupon uc : ucs) couponIds.add(uc.getCouponId());
        Map<Long, SmsCoupon> couponMap = new HashMap<>();
        for (Long cid : couponIds) {
            SmsCoupon c = couponMapper.selectOneById(cid);
            if (c != null) couponMap.put(cid, c);
        }

        List<MatchedCouponVO> result = new ArrayList<>();
        for (SmsUserCoupon uc : ucs) {
            SmsCoupon c = couponMap.get(uc.getCouponId());
            if (c == null || c.getStatus() == null || c.getStatus() != CouponStatus.COUPON_ENABLED) continue;

            boolean scopeOk = scopeValidator.matches(c, spuIds, categoryIds, merchantId);
            BigDecimal threshold = c.getThreshold() != null ? c.getThreshold() : BigDecimal.ZERO;
            boolean thresholdOk = totalAmount.compareTo(threshold) >= 0;
            boolean usable = scopeOk && thresholdOk;

            String reason = !scopeOk ? "优惠券不适用于当前商品" : (!thresholdOk ? "未满" + threshold.toPlainString() + "元" : "");

            result.add(MatchedCouponVO.builder()
                    .userCouponId(uc.getId()).couponId(uc.getCouponId())
                    .name(c.getName()).type(c.getType()).typeText(typeText(c.getType()))
                    .threshold(threshold.toPlainString())
                    .discountAmount(usable ? calcDiscount(c, totalAmount).toPlainString() : "0")
                    .discountText(discountText(c))
                    .scopeType(c.getScopeType()).scopeValue(c.getScopeValue())
                    .usable(usable).reason(reason)
                    .build());
        }
        result.sort((a, b) -> Boolean.compare(!a.isUsable(), !b.isUsable()));
        return result;
    }

    private BigDecimal calcDiscount(com.clmcat.qianyu.mall.coupon.model.entity.SmsCoupon c, BigDecimal total) {
        if (c.getType() == null) return BigDecimal.ZERO;
        return switch (c.getType()) {
            case 1 -> (c.getDiscountAmount() != null ? c.getDiscountAmount() : BigDecimal.ZERO).min(total);
            case 2 -> total.multiply(BigDecimal.ONE.subtract(
                    (c.getDiscountRate() != null ? c.getDiscountRate() : BigDecimal.TEN)
                    .divide(BigDecimal.TEN, 4, RoundingMode.HALF_UP))).setScale(2, RoundingMode.HALF_UP);
            case 3 -> BigDecimal.ZERO; // 免运费（运费抵扣在 PriceService）
            default -> BigDecimal.ZERO;
        };
    }

    private String typeText(Integer t) { return t == null ? "" : switch(t){case 1->"满减";case 2->"折扣";case 3->"免运费";default->"";}; }
    private String discountText(SmsCoupon c) {
        if (c == null || c.getType() == null) return "";
        return switch(c.getType()) {
            case 1 -> "减" + (c.getDiscountAmount()!=null?c.getDiscountAmount().toPlainString():"0") + "元";
            case 2 -> "打" + (c.getDiscountRate()!=null?c.getDiscountRate().toPlainString():"0") + "折";
            case 3 -> "免运费";
            default -> "";
        };
    }
}
