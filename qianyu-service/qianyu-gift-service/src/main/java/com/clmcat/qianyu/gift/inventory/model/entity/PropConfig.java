package com.clmcat.qianyu.gift.inventory.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 道具配置表（静态定义）。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("prop_config")
public class PropConfig {

    /** 道具ID（雪花） */
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;

    /** 道具名称 */
    @Column("name")
    private String name;

    /** 图标URL */
    @Column("icon")
    private String icon;

    /** 道具类型：mount/frame/bubble/tag/title/other */
    @Column("prop_type")
    private String propType;

    /** 1=穿戴（可穿脱） 2=消耗（使用即销毁） */
    @Column("usage_type")
    private Integer usageType;

    /** 特效URL（座驾进场动画等） */
    @Column("animation_url")
    private String animationUrl;

    /** 扩展配置JSON */
    @Column("extra_config")
    private String extraConfig;

    /** 有效天数，0=永久 */
    @Column("valid_days")
    private Integer validDays;

    /** 0=下架 1=上架 */
    @Column("status")
    private Integer status;

    /** 创建时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    /** 更新时间戳（毫秒） */
    @Column("update_time")
    private Long updateTime;

    // ===== 常量 =====
    public static final int USAGE_TYPE_WEAR     = 1;
    public static final int USAGE_TYPE_CONSUME  = 2;

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED  = 1;

    public static final String TYPE_MOUNT  = "mount";
    public static final String TYPE_FRAME  = "frame";
    public static final String TYPE_BUBBLE = "bubble";
    public static final String TYPE_TAG    = "tag";
    public static final String TYPE_TITLE  = "title";
    public static final String TYPE_OTHER  = "other";
}
