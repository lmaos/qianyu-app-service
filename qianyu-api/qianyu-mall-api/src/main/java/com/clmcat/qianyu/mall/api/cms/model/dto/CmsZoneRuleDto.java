package com.clmcat.qianyu.mall.api.cms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 楼层投放规则 DTO。ruleParams 在实体为 JSON，此处摊平为顶层字段便于前端绑定。
 */
@Data
public class CmsZoneRuleDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long zoneId;
    private String name;
    /** NEW_PRODUCT / HIGH_SALES / BY_CATEGORY / KEYWORD */
    private String ruleType;
    /** HIGH_SALES：销量阈值 */
    private Integer threshold;
    /** BY_CATEGORY：分类 ID */
    private Long categoryId;
    /** NEW_PRODUCT：近 N 天 */
    private Integer days;
    /** KEYWORD：关键词 */
    private String keyword;
    private Integer sort;
    /** 0=启用 1=停用 */
    private Integer status;
    private Long createTime;
}
