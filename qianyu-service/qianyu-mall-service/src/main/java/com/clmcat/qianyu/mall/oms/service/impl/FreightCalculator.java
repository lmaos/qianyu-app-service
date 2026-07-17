package com.clmcat.qianyu.mall.oms.service.impl;

import com.clmcat.qianyu.mall.mch.rpc.MerchantFreightApiImpl;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantFreightTemplate;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantFreightRule;
import com.clmcat.qianyu.mall.oms.model.vo.FreightContext;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Component
public class FreightCalculator {

    @Resource
    private MerchantFreightApiImpl freightApi;
    @Resource
    private PmsSpuMapper spuMapper;

    public BigDecimal calc(FreightContext ctx, BigDecimal totalAmount) {
        if (ctx == null || ctx.getItems() == null || ctx.getItems().isEmpty()) return BigDecimal.ZERO;

        // 1. 收集 spuId → 取 freightTemplateId
        Set<Long> spuIds = new HashSet<>();
        for (FreightContext.FreightItem item : ctx.getItems()) {
            if (item.getSpuId() != null) spuIds.add(item.getSpuId());
        }
        if (spuIds.isEmpty()) return BigDecimal.ZERO;

        Long templateId = null;
        for (Long spuId : spuIds) {
            PmsSpu spu = spuMapper.selectOneById(spuId);
            if (spu != null && spu.getFreightTemplateId() != null && spu.getFreightTemplateId() > 0) {
                templateId = spu.getFreightTemplateId();
                break;
            }
        }
        if (templateId == null) {
            // 所有 SPU 都没绑模板 → 查商家默认模板
            List<MerchantFreightTemplate> templates = freightApi.selectTemplatesByMerchantId(ctx.getMerchantId());
            if (templates != null) {
                for (MerchantFreightTemplate t : templates) {
                    if (t.getIsDefault() != null && t.getIsDefault() == 1 && t.getStatus() != null && t.getStatus() == 1) {
                        templateId = t.getId();
                        break;
                    }
                }
            }
        }
        if (templateId == null) return BigDecimal.ZERO; // 无模板 = 免运费

        // 2. 取模板
        MerchantFreightTemplate template = freightApi.selectTemplateOneById(templateId);
        if (template == null || template.getStatus() == null || template.getStatus() != 1) return BigDecimal.ZERO;

        // 3. 包邮判断
        int freeType = template.getFreeShippingType() != null ? template.getFreeShippingType() : 0;
        BigDecimal freeValue = template.getFreeShippingValue();
        if (freeType == 1 && freeValue != null && totalAmount.compareTo(freeValue) >= 0) {
            return BigDecimal.ZERO; // 满额包邮
        }
        int totalQty = ctx.getItems().stream().mapToInt(FreightContext.FreightItem::getQuantity).sum();
        if (freeType == 2 && freeValue != null && BigDecimal.valueOf(totalQty).compareTo(freeValue) >= 0) {
            return BigDecimal.ZERO; // 满件包邮
        }

        // 4. 取规则 → 匹配区域
        List<MerchantFreightRule> rules = null;
        try {
            rules = freightApi.selectRulesByTemplateId(templateId);
        } catch (Exception e) {
            log.warn("[freight] 规则反序列化失败 templateId={}，降级免运费", templateId, e);
            return BigDecimal.ZERO;
        }
        if (rules == null || rules.isEmpty()) return BigDecimal.ZERO;

        MerchantFreightRule matchedRule = matchRule(rules, ctx.getProvince());
        if (matchedRule == null) return BigDecimal.ZERO;

        // 5. 按 billingType 算运费
        int billingType = template.getBillingType() != null ? template.getBillingType() : 1;
        BigDecimal chargeableAmount;
        switch (billingType) {
            case 2: // 按重量
                BigDecimal totalWeight = BigDecimal.ZERO;
                for (FreightContext.FreightItem item : ctx.getItems()) {
                    BigDecimal w = item.getWeight() != null ? item.getWeight() : BigDecimal.ZERO;
                    totalWeight = totalWeight.add(w.multiply(BigDecimal.valueOf(item.getQuantity())));
                }
                chargeableAmount = totalWeight;
                break;
            case 3: // 按体积
                BigDecimal totalVolume = BigDecimal.ZERO;
                for (FreightContext.FreightItem item : ctx.getItems()) {
                    BigDecimal v = item.getVolume() != null ? item.getVolume() : BigDecimal.ZERO;
                    totalVolume = totalVolume.add(v.multiply(BigDecimal.valueOf(item.getQuantity())));
                }
                chargeableAmount = totalVolume;
                break;
            default: // 1=按件
                chargeableAmount = BigDecimal.valueOf(totalQty);
        }

        return calcFreight(chargeableAmount, matchedRule);
    }

    private MerchantFreightRule matchRule(List<MerchantFreightRule> rules, String province) {
        MerchantFreightRule defaultRule = null;
        if (province != null) {
            for (MerchantFreightRule r : rules) {
                int destType = r.getDestinationType() != null ? r.getDestinationType() : 1;
                if (destType == 2) {
                    try {
                        List<String> dest = r.getDestination();
                        if (dest != null && dest.contains(province)) return r;
                    } catch (Exception e) { /* 防御：destination 格式不匹配 */ }
                }
            }
        }
        for (MerchantFreightRule r : rules) {
            int destType = r.getDestinationType() != null ? r.getDestinationType() : 1;
            if (destType == 1) { defaultRule = r; break; }
        }
        return defaultRule;
    }

    private BigDecimal calcFreight(BigDecimal amount, MerchantFreightRule rule) {
        BigDecimal firstUnit = rule.getFirstUnit() != null ? rule.getFirstUnit() : BigDecimal.ONE;
        BigDecimal firstPrice = rule.getFirstPrice() != null ? rule.getFirstPrice() : BigDecimal.ZERO;
        BigDecimal addUnit = rule.getAdditionalUnit() != null && rule.getAdditionalUnit().compareTo(BigDecimal.ZERO) > 0
                ? rule.getAdditionalUnit() : BigDecimal.ONE;
        BigDecimal addPrice = rule.getAdditionalPrice() != null ? rule.getAdditionalPrice() : BigDecimal.ZERO;

        if (amount.compareTo(firstUnit) <= 0) return firstPrice;
        BigDecimal extra = amount.subtract(firstUnit).divide(addUnit, 0, RoundingMode.CEILING);
        return firstPrice.add(extra.multiply(addPrice));
    }
}
