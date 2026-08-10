package com.clmcat.qianyu.live.api.room.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 直播间列表（RPC 返回），含游标分页。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomListDto implements Serializable {

    /** 空列表单例 */
    public static final LiveRoomListDto EMPTY = new LiveRoomListDto(
            Collections.emptyList(), 0L, false);

    /** 直播间列表 */
    private List<LiveRoomDto> rooms;

    /** 下一页游标（取最后一条的 roomNo，0 表示没有更多） */
    private Long nextNo;

    /** 是否还有更多 */
    private Boolean hasMore;
}
