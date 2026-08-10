package com.clmcat.qianyu.live.room.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 直播间列表视图对象（返回前端）。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomListVo {

    /** 空列表单例 */
    public static final LiveRoomListVo EMPTY = LiveRoomListVo.builder()
            .rooms(Collections.emptyList())
            .nextNo(0L)
            .hasMore(false)
            .build();

    /** 直播间列表 */
    private List<LiveRoomVo> rooms;

    /** 下一页游标（取最后一条的 roomNo，0 表示没有更多） */
    private Long nextNo;

    /** 是否还有更多 */
    private Boolean hasMore;
}
