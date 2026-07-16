package com.clmcat.qianyu.mall.cms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

/**
 * 楼层自动投放规则。由 {@code RuleBasedZonePlacementDecider} 在 SPU 上架时评估，
 * 命中（同楼层多规则为「或」）即把 SPU 写入 {@code cms_zone_product}(source=AUTO)。
 */
@Data
@Table("cms_zone_rule")
public class CmsZoneRule {

    public static final int STATUS_ENABLED = 0;
    public static final int STATUS_DISABLED = 1;

    /** rule_type：新品（publish_time 近 days 天） */
    public static final String TYPE_NEW_PRODUCT = "NEW_PRODUCT";
    /** rule_type：高销量（sales ≥ threshold） */
    public static final String TYPE_HIGH_SALES = "HIGH_SALES";
    /** rule_type：指定分类（categoryId 精确匹配） */
    public static final String TYPE_BY_CATEGORY = "BY_CATEGORY";
    /** rule_type：关键词（name 含 keyword） */
    public static final String TYPE_KEYWORD = "KEYWORD";

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "zone_id", comment = "楼层 ID")
    private Long zoneId;

    @Column(value = "name", comment = "规则名称")
    private String name;

    @Column(value = "rule_type", comment = "NEW_PRODUCT/HIGH_SALES/BY_CATEGORY/KEYWORD")
    private String ruleType;

    @Column(value = "rule_params", comment = "规则参数 JSON", typeHandler = JacksonTypeHandler.class)
    private RuleParams ruleParams;

    @Column(value = "sort", comment = "排序")
    private Integer sort;

    @Column(value = "status", comment = "0=启用 1=停用")
    private Integer status;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除 1=已删除", isLogicDelete = true)
    private Integer deleted;

    /** 规则参数（按 rule_type 取用对应字段）。 */
    @Data
    public static class RuleParams {
        /** HIGH_SALES：销量阈值 */
        private Integer threshold;
        /** BY_CATEGORY：分类 ID */
        private Long categoryId;
        /** NEW_PRODUCT：近 N 天（按 publish_time） */
        private Integer days;
        /** KEYWORD：名称包含的关键词 */
        private String keyword;
    }
}
