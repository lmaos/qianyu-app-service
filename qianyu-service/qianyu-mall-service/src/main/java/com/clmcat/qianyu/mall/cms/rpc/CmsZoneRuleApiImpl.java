package com.clmcat.qianyu.mall.cms.rpc;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.mall.api.cms.CmsZoneRuleApi;
import com.clmcat.qianyu.mall.api.cms.model.dto.CmsZoneRuleDto;
import com.clmcat.qianyu.mall.cms.mapper.CmsZoneRuleMapper;
import com.clmcat.qianyu.mall.cms.model.entity.CmsZoneRule;
import com.clmcat.qianyu.mall.pms.support.PmsSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.clmcat.qianyu.mall.cms.model.entity.table.CmsZoneRuleTableDef.CMS_ZONE_RULE;

/**
 * 楼层投放规则管理 RPC 实现。
 */
@DubboService
@Service
public class CmsZoneRuleApiImpl implements CmsZoneRuleApi {

    @Resource
    private CmsZoneRuleMapper zoneRuleMapper;
    @Resource
    private PmsSupport pmsSupport;

    @Override
    public List<CmsZoneRuleDto> listByZone(Long zoneId) {
        List<CmsZoneRule> rules = zoneRuleMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(CMS_ZONE_RULE.ZONE_ID.eq(zoneId))
                        .orderBy(CMS_ZONE_RULE.SORT.asc(), CMS_ZONE_RULE.ID.asc()));
        List<CmsZoneRuleDto> result = new ArrayList<>();
        if (rules == null) {
            return result;
        }
        for (CmsZoneRule r : rules) {
            result.add(toDto(r));
        }
        return result;
    }

    @Override
    public Long create(CmsZoneRuleDto dto) {
        long id = pmsSupport.nextId();
        CmsZoneRule r = new CmsZoneRule();
        r.setId(id);
        r.setZoneId(dto.getZoneId());
        r.setName(dto.getName());
        r.setRuleType(dto.getRuleType());
        r.setRuleParams(buildParams(dto));
        r.setSort(dto.getSort() != null ? dto.getSort() : 0);
        r.setStatus(dto.getStatus() != null ? dto.getStatus() : CmsZoneRule.STATUS_ENABLED);
        r.setCreateTime(pmsSupport.parseTime(id));
        r.setUpdateTime(System.currentTimeMillis());
        r.setDeleted(0);
        zoneRuleMapper.insertSelective(r);
        return id;
    }

    @Override
    public void update(CmsZoneRuleDto dto) {
        CmsZoneRule exist = zoneRuleMapper.selectOneById(dto.getId());
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx("规则不存在", exist == null);
        CmsZoneRule u = new CmsZoneRule();
        u.setId(dto.getId());
        if (dto.getName() != null) u.setName(dto.getName());
        if (dto.getRuleType() != null) {
            u.setRuleType(dto.getRuleType());
            u.setRuleParams(buildParams(dto)); // 类型变更时整体重置参数
        } else if (hasAnyParam(dto)) {
            u.setRuleParams(buildParams(dto)); // 仅改参数
        }
        if (dto.getSort() != null) u.setSort(dto.getSort());
        if (dto.getStatus() != null) u.setStatus(dto.getStatus());
        u.setUpdateTime(System.currentTimeMillis());
        zoneRuleMapper.update(u);
    }

    @Override
    public void delete(Long id) {
        CmsZoneRule u = new CmsZoneRule();
        u.setId(id);
        u.setDeleted(1);
        u.setUpdateTime(System.currentTimeMillis());
        zoneRuleMapper.update(u);
    }

    @Override
    public void setStatus(Long id, int status) {
        CmsZoneRule u = new CmsZoneRule();
        u.setId(id);
        u.setStatus(status);
        u.setUpdateTime(System.currentTimeMillis());
        zoneRuleMapper.update(u);
    }

    // ==================== helpers ====================

    private CmsZoneRule.RuleParams buildParams(CmsZoneRuleDto dto) {
        CmsZoneRule.RuleParams p = new CmsZoneRule.RuleParams();
        p.setThreshold(dto.getThreshold());
        p.setCategoryId(dto.getCategoryId());
        p.setDays(dto.getDays());
        p.setKeyword(dto.getKeyword());
        return p;
    }

    private boolean hasAnyParam(CmsZoneRuleDto dto) {
        return dto.getThreshold() != null || dto.getCategoryId() != null
                || dto.getDays() != null || dto.getKeyword() != null;
    }

    private CmsZoneRuleDto toDto(CmsZoneRule r) {
        CmsZoneRuleDto d = new CmsZoneRuleDto();
        d.setId(r.getId());
        d.setZoneId(r.getZoneId());
        d.setName(r.getName());
        d.setRuleType(r.getRuleType());
        d.setSort(r.getSort());
        d.setStatus(r.getStatus());
        d.setCreateTime(r.getCreateTime());
        CmsZoneRule.RuleParams p = r.getRuleParams();
        if (p != null) {
            d.setThreshold(p.getThreshold());
            d.setCategoryId(p.getCategoryId());
            d.setDays(p.getDays());
            d.setKeyword(p.getKeyword());
        }
        return d;
    }
}
