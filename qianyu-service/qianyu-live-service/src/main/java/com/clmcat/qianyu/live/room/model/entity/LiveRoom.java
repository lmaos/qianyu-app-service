package com.clmcat.qianyu.live.room.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直播间主表（低频写）。
 * <p>
 * id 为内部逻辑主键（雪花ID，永不变），room_no 为对外直播间编号（默认等于 id，
 * 后续可扩展分配短号/靓号）。其他表关联直播间时使用 id（room_id → live_room.id），
 * 外部分享、搜索均基于 room_no。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("live_room")
public class LiveRoom {

    /** 内部逻辑主键（雪花ID），永远不变 */
    @Id(keyType = KeyType.None)
    private Long id;

    /** 对外直播间编号，默认 == id。UNIQUE，用于搜索/分享/链接 */
    @Column("room_no")
    private Long roomNo;

    /** 主播用户ID */
    @Column("anchor_user_id")
    private Long anchorUserId;

    /** 直播间标题 */
    @Column("title")
    private String title;

    /** 封面图 URL */
    @Column("cover_image")
    private String coverImage;

    /** 状态：0=待开播，1=直播中，2=已结束 */
    @Column("status")
    private Integer status;

    /** 开播时间戳（毫秒） */
    @Column("start_time")
    private Long startTime;

    /** 关播时间戳（毫秒） */
    @Column("end_time")
    private Long endTime;

    /** 创建时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    /** 更新时间戳（毫秒） */
    @Column("update_time")
    private Long updateTime;

    // ---- 状态常量 ----

    /** 待开播 */
    public static final int STATUS_PENDING = 0;
    /** 直播中 */
    public static final int STATUS_LIVE = 1;
    /** 已结束 */
    public static final int STATUS_CLOSED = 2;
}
