package com.clmcat.qianyu.gift.gift.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 礼物配置表（静态定义，所有礼物类型共用）。
 * <p>
 * 差异化行为通过 {@code giftType} + {@code extraConfig} JSON 驱动。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("gift_config")
public class GiftConfig {

    /** 礼物ID（雪花） */
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;

    /** 礼物名称 */
    @Column("name")
    private String name;

    /** 图标URL */
    @Column("icon")
    private String icon;

    /** 动画URL（mp4/webp） */
    @Column("animation_url")
    private String animationUrl;

    /** 基础价格（虚拟币最小单位），0=免费/活动获取 */
    @Column("price")
    private Long price;

    /** 礼物类型：1=普通 2=盲盒 3=奖池 4=专属 5=活动 6=锁定 7=条件 8=任务 */
    @Column("gift_type")
    private Integer giftType;

    /** 上架分类：luxury=豪华, normal=普通, special=特殊, free=免费 */
    @Column("category")
    private String category;

    /** 扩展配置JSON */
    @Column("extra_config")
    private String extraConfig;

    /** 上架场景，逗号分隔 */
    @Column("shelf_scenes")
    private String shelfScenes;

    /** 排序权重 */
    @Column("sort_order")
    private Integer sortOrder;

    /** 0=下架 1=上架 */
    @Column("status")
    private Integer status;

    /** 默认分佣比例（万分比） */
    @Column("commission_rate")
    private Integer commissionRate;

    /** 动画时长（毫秒） */
    @Column("animation_duration")
    private Integer animationDuration;

    /** SVGA动画URL（备选） */
    @Column("svga_url")
    private String svgaUrl;

    /** 创建时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    /** 更新时间戳（毫秒） */
    @Column("update_time")
    private Long updateTime;

    // ===== 常量 =====
    public static final int TYPE_NORMAL      = 1;
    public static final int TYPE_BLINDBOX    = 2;
    public static final int TYPE_LOTTERY     = 3;
    public static final int TYPE_EXCLUSIVE   = 4;
    public static final int TYPE_EVENT       = 5;
    public static final int TYPE_LOCKED      = 6;
    public static final int TYPE_CONDITIONAL = 7;
    public static final int TYPE_MISSION     = 8;

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED  = 1;

    public static final String CATEGORY_LUXURY  = "luxury";
    public static final String CATEGORY_NORMAL  = "normal";
    public static final String CATEGORY_SPECIAL = "special";
    public static final String CATEGORY_FREE    = "free";
}
