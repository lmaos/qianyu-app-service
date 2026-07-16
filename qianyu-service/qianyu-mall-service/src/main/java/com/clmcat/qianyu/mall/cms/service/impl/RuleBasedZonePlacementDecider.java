package com.clmcat.qianyu.mall.cms.service.impl;

import com.clmcat.qianyu.mall.cms.mapper.CmsZoneRuleMapper;
import com.clmcat.qianyu.mall.cms.model.entity.CmsZoneRule;
import com.clmcat.qianyu.mall.cms.service.ZonePlacementDecider;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsZoneRuleTableDef.CMS_ZONE_RULE;

/**
 * 基于规则的楼层投放决策器（{@link ZonePlacementDecider} 唯一实现，替代原 Noop）。
 *
 * <p>「或」语义：同一楼层配置多条规则时，SPU 命中其中<b>任一条</b>即投放进该楼层。
 * 规则集通常很小，每次 decide 查一次启用规则（后续可加内存缓存）。
 *
 * <p>规则类型见 {@link CmsZoneRule}：NEW_PRODUCT(新品) / HIGH_SALES(高销量) / BY_CATEGORY(分类) / KEYWORD(关键词)。
 */
@Service
public class RuleBasedZonePlacementDecider implements ZonePlacementDecider {

    @Resource
    private CmsZoneRuleMapper zoneRuleMapper;

    @Override
    public List<Placement> decide(PmsSpu spu) {
        List<Placement> result = new ArrayList<>();
        if (spu == null || spu.getId() == null) {
            return result;
        }
        List<CmsZoneRule> rules = zoneRuleMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(CMS_ZONE_RULE.STATUS.eq(CmsZoneRule.STATUS_ENABLED))
                        .orderBy(CMS_ZONE_RULE.SORT.asc(), CMS_ZONE_RULE.ID.asc()));
        if (rules == null || rules.isEmpty()) {
            return result;
        }
        // 按 zone 分组；同一 zone 命中任一规则即投放（或语义）。用 LinkedHashMap 保序 + 去重 zone。
        Map<Long, Placement> hit = new LinkedHashMap<>();
        for (CmsZoneRule rule : rules) {
            if (match(rule, spu)) {
                hit.computeIfAbsent(rule.getZoneId(), z -> new Placement(z, 0));
            }
        }
        result.addAll(hit.values());
        return result;
    }

    private boolean match(CmsZoneRule rule, PmsSpu spu) {
        CmsZoneRule.RuleParams p = rule.getRuleParams();
        if (p == null) {
            return false;
        }
        switch (rule.getRuleType()) {
            case CmsZoneRule.TYPE_NEW_PRODUCT: {
                Integer days = p.getDays();
                if (days == null || days <= 0 || spu.getPublishTime() == null) {
                    return false;
                }
                long threshold = System.currentTimeMillis() - days * 86400000L;
                return spu.getPublishTime() >= threshold;
            }
            case CmsZoneRule.TYPE_HIGH_SALES: {
                Integer threshold = p.getThreshold();
                Integer sales = spu.getSales();
                return threshold != null && sales != null && sales >= threshold;
            }
            case CmsZoneRule.TYPE_BY_CATEGORY: {
                return p.getCategoryId() != null && p.getCategoryId().equals(spu.getCategoryId());
            }
            case CmsZoneRule.TYPE_KEYWORD: {
                String kw = p.getKeyword();
                String name = spu.getName();
                return kw != null && !kw.isBlank() && name != null && name.contains(kw);
            }
            default:
                return false;
        }
    }
}
