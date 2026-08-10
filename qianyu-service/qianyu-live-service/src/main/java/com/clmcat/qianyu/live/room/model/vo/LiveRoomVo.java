package com.clmcat.qianyu.live.room.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直播间视图对象（返回前端）。
 * <p>
 * 后续可扩展聚合主播昵称/头像等额外信息。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomVo {

    /** 对外直播间编号 */
    private Long roomNo;

    /** 主播用户ID */
    private Long anchorUserId;

    /** 直播间标题 */
    private String title;

    /** 封面图 URL */
    private String coverImage;

    /** 状态：0=待开播，1=直播中，2=已结束 */
    private Integer status;

    /** 本场观看人数 */
    private Long viewerCount;

    /** 本场点赞数 */
    private Long likeCount;

    /** 开播时间戳（毫秒） */
    private Long startTime;

    /** 关播时间戳（毫秒） */
    private Long endTime;
}
