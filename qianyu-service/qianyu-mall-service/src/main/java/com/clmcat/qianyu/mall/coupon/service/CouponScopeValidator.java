package com.clmcat.qianyu.mall.coupon.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsCoupon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class CouponScopeValidator {

    public boolean matches(SmsCoupon coupon, List<Long> spuIds, List<Long> categoryIds, Long merchantId) {
        if (coupon == null || coupon.getScopeType() == null) return true;
        int scopeType = coupon.getScopeType();
        if (scopeType == 1) return true; // 全平台

        List<Long> scopeValues = parseScopeValue(coupon.getScopeValue());
        if (scopeType == 2) { // 指定商家
            if (scopeValues.isEmpty()) return coupon.getMerchantId() != null && coupon.getMerchantId().equals(merchantId);
            return merchantId != null && scopeValues.contains(merchantId);
        }
        if (scopeType == 3) { // 指定品类
            if (categoryIds == null || categoryIds.isEmpty()) return false;
            return scopeValues.stream().anyMatch(categoryIds::contains);
        }
        if (scopeType == 4) { // 指定商品
            if (spuIds == null || spuIds.isEmpty()) return false;
            return scopeValues.stream().anyMatch(spuIds::contains);
        }
        return true;
    }

    private List<Long> parseScopeValue(String scopeValue) {
        if (scopeValue == null || scopeValue.isBlank()) return Collections.emptyList();
        try {
            // 兼容三种格式：null / [id,id] / {"categoryIds":[id,id]}
            String trimmed = scopeValue.trim();
            if (trimmed.startsWith("{")) {
                JSONArray arr = JSON.parseObject(trimmed).getJSONArray("categoryIds");
                return arr != null ? toLongList(arr) : Collections.emptyList();
            }
            JSONArray arr = JSON.parseArray(trimmed);
            return toLongList(arr);
        } catch (Exception e) {
            log.warn("[coupon] scope_value 解析失败: {}", scopeValue, e);
            return Collections.emptyList();
        }
    }

    private List<Long> toLongList(JSONArray arr) {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) result.add(arr.getLong(i));
        return result;
    }
}
