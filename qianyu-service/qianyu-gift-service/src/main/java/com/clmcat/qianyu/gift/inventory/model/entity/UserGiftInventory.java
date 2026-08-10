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
 * 用户礼物库存表（背包）。
 * <p>
 * 聚合存储：同一用户 + 同一礼物 + 同一过期日期 = 一行。
 * 发放时通过 INSERT ON DUPLICATE KEY UPDATE 累加数量。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_gift_inventory")
public class UserGiftInventory {

    /** 库存记录ID（雪花） */
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;

    /** 用户ID */
    @Column("user_id")
    private Long userId;

    /** 礼物ID */
    @Column("gift_id")
    private Long giftId;

    /** 持有数量 */
    @Column("quantity")
    private Integer quantity;

    /** 来源：activity/task/event/admin_grant/refund */
    @Column("source_type")
    private String sourceType;

    /** 来源单号 */
    @Column("source_id")
    private String sourceId;

    /** 过期时间戳（毫秒），0=永久 */
    @Column("expire_time")
    private Long expireTime;

    /** 1=有效 2=已过期 3=已用完 */
    @Column("status")
    private Integer status;

    /** 创建时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    /** 更新时间戳（毫秒） */
    @Column("update_time")
    private Long updateTime;

    // ===== 常量 =====
    public static final int STATUS_VALID   = 1;
    public static final int STATUS_EXPIRED = 2;
    public static final int STATUS_USED_UP = 3;

    public static final String SOURCE_ACTIVITY    = "activity";
    public static final String SOURCE_TASK        = "task";
    public static final String SOURCE_EVENT       = "event";
    public static final String SOURCE_ADMIN_GRANT = "admin_grant";
    public static final String SOURCE_REFUND      = "refund";
    public static final String SOURCE_BLINDBOX    = "blindbox";
}
