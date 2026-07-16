package com.clmcat.qianyu.mall.api.cms;

import com.clmcat.qianyu.mall.api.cms.model.dto.CmsZoneRuleDto;

import java.util.List;

/**
 * CMS 楼层投放规则管理 RPC 接口 — 供运营后台管理 {@code cms_zone_rule}。
 * 规则由 {@code RuleBasedZonePlacementDecider} 在 SPU 上架时评估（同楼层多规则为「或」）。
 */
public interface CmsZoneRuleApi {

    /** 某楼层的规则列表（按 sort 升序）。 */
    List<CmsZoneRuleDto> listByZone(Long zoneId);

    /** 新建规则，返回规则 ID。 */
    Long create(CmsZoneRuleDto dto);

    /** 更新规则（id 必填）。 */
    void update(CmsZoneRuleDto dto);

    /** 删除规则（逻辑删除）。 */
    void delete(Long id);

    /** 切换规则启停：status 0=启用 1=停用。 */
    void setStatus(Long id, int status);
}
