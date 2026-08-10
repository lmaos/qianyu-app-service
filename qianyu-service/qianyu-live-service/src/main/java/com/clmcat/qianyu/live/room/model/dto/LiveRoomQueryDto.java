package com.clmcat.qianyu.live.room.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直播间列表查询参数（Controller 层参数对象）。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomQueryDto {

    /** 上一页最后一条的 roomNo，首次传 0 */
    private Long nextNo;

    /** 每页条数 */
    private Integer limit;
}
