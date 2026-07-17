package com.clmcat.qianyu.mall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.clmcat.qianyu.mall.promotion.mapper.SmsPromotionMapper;
import com.clmcat.qianyu.mall.promotion.model.entity.SmsPromotion;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class PromotionCalculator {

    @Resource
    private SmsPromotionMapper promotionMapper;

    /**
     * 计算满减抵扣。
     * @param merchantId 商家 ID
     * @param totalAmount 商品总额（不含运费）
     * @return 满减抵扣金额（不超过 totalAmount）
     */
    public BigDecimal calc(Long merchantId, BigDecimal totalAmount) {
        if (merchantId == null || totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        long now = System.currentTimeMillis();
        List<SmsPromotion> promos = promotionMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("deleted = 0").and("status = 1").and("type = 4")
                        .and("merchant_id = ?", merchantId)
                        .and("start_time <= ?", now).and("end_time >= ?", now));
        if (promos == null || promos.isEmpty()) return BigDecimal.ZERO;

        BigDecimal bestDiscount = BigDecimal.ZERO;
        for (SmsPromotion p : promos) {
            BigDecimal d = calcSinglePromo(p, totalAmount);
            if (d.compareTo(bestDiscount) > 0) bestDiscount = d;
        }
        return bestDiscount.min(totalAmount);
    }

    private BigDecimal calcSinglePromo(SmsPromotion promo, BigDecimal totalAmount) {
        try {
            JSONObject rules = JSON.parseObject(promo.getRules());
            if (rules == null) return BigDecimal.ZERO;
            JSONArray steps = rules.getJSONArray("steps");
            if (steps == null || steps.isEmpty()) return BigDecimal.ZERO;

            BigDecimal matched = BigDecimal.ZERO;
            for (int i = 0; i < steps.size(); i++) {
                JSONObject step = steps.getJSONObject(i);
                BigDecimal threshold = step.getBigDecimal("threshold");
                BigDecimal discount = step.getBigDecimal("discount");
                if (threshold != null && discount != null && totalAmount.compareTo(threshold) >= 0) {
                    if (discount.compareTo(matched) > 0) matched = discount;
                }
            }
            return matched;
        } catch (Exception e) {
            log.warn("[promotion] 活动规则解析失败 id={} rules={}", promo.getId(), promo.getRules(), e);
            return BigDecimal.ZERO;
        }
    }
}
