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
 * 盲盒掉落配置表。
 * <p>
 * 每个盲盒礼物可配置多个掉落项，按权重随机开出。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("gift_blindbox_drop")
public class GiftBlindboxDrop {

    /** ID（雪花） */
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;

    /** 盲盒礼物ID */
    @Column("blindbox_gift_id")
    private Long blindboxGiftId;

    /** 可开出礼物ID */
    @Column("drop_gift_id")
    private Long dropGiftId;

    /** 权重（越大概率越高） */
    @Column("weight")
    private Integer weight;

    /** 1=启用 2=停用 */
    @Column("status")
    private Integer status;

    /** 创建时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    // ===== 常量 =====
    public static final int STATUS_ENABLED  = 1;
    public static final int STATUS_DISABLED = 2;
}
