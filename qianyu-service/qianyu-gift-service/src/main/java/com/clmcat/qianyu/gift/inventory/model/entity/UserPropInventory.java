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
 * 用户道具库存表。
 * <p>
 * 道具一个是一个，不聚合。同类型穿戴互斥。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_prop_inventory")
public class UserPropInventory {

    /** 道具记录ID（雪花） */
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;

    /** 用户ID */
    @Column("user_id")
    private Long userId;

    /** 道具配置ID */
    @Column("prop_id")
    private Long propId;

    /** 道具类型（冗余，方便查询） */
    @Column("prop_type")
    private String propType;

    /** 0=背包中 1=穿戴中 2=已使用 3=已过期 */
    @Column("status")
    private Integer status;

    /** 来源 */
    @Column("source_type")
    private String sourceType;

    /** 来源单号 */
    @Column("source_id")
    private String sourceId;

    /** 获取时间戳（毫秒） */
    @Column("obtain_time")
    private Long obtainTime;

    /** 过期时间戳（毫秒），0=永久 */
    @Column("expire_time")
    private Long expireTime;

    /** 创建时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    /** 更新时间戳（毫秒） */
    @Column("update_time")
    private Long updateTime;

    // ===== 常量 =====
    public static final int STATUS_IN_BACKPACK = 0;
    public static final int STATUS_EQUIPPED    = 1;
    public static final int STATUS_USED        = 2;
    public static final int STATUS_EXPIRED     = 3;

    public static final String SOURCE_ACTIVITY      = "activity";
    public static final String SOURCE_VIP_UPGRADE   = "vip_upgrade";
    public static final String SOURCE_ADMIN_GRANT   = "admin_grant";
}
