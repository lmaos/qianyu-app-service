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
 * 直播间计数器表（高频写）。
 * <p>
 * 本场计数，开播时清零。与 live_room 主表拆分，避免计数器高频 UPDATE 与
 * 房间主信息读写产生行锁竞争。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("live_room_count")
public class LiveRoomCount {

    /** 关联 live_room.id（内部逻辑主键） */
    @Id(keyType = KeyType.None)
    private Long roomId;

    /** 本场观看人数 */
    @Column("viewer_count")
    private Long viewerCount;

    /** 本场峰值在线人数 */
    @Column("max_online_count")
    private Long maxOnlineCount;

    /** 本场点赞数 */
    @Column("like_count")
    private Long likeCount;

    /** 本场礼物数 */
    @Column("gift_count")
    private Long giftCount;

    /** 本场礼物金额（最小单位） */
    @Column("gift_amount")
    private Long giftAmount;

    /** 本场评论/弹幕数 */
    @Column("comment_count")
    private Long commentCount;

    /** 本场分享次数 */
    @Column("share_count")
    private Long shareCount;
}
