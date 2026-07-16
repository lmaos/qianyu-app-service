package com.clmcat.qianyu.mall.cms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 楼层-商品关联（承载手动选品 source=0 与自动投放 source=1 的落点）。
 */
@Data
@Table("cms_zone_product")
public class CmsZoneProduct {

    /** source：手动选品（运营在后台挑的） */
    public static final int SOURCE_MANUAL = 0;
    /** source：自动投放（定时任务按规则写入） */
    public static final int SOURCE_AUTO = 1;

    /** status：显示 */
    public static final int STATUS_SHOW = 0;
    /** status：隐藏 */
    public static final int STATUS_HIDE = 1;

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "zone_id", comment = "楼层 ID")
    private Long zoneId;

    @Column(value = "spu_id", comment = "商品 SPU ID")
    private Long spuId;

    @Column(value = "sort", comment = "楼层内排序（升序）")
    private Integer sort;

    @Column(value = "status", comment = "0=显示 1=隐藏")
    private Integer status;

    @Column(value = "source", comment = "0=手动 1=自动")
    private Integer source;

    @Column(value = "rule_id", comment = "自动来源规则 ID（预留）")
    private Long ruleId;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
