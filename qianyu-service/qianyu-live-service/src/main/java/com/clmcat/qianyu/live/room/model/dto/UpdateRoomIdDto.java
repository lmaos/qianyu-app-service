package com.clmcat.qianyu.live.room.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新直播间对外编号参数（Controller 层参数对象）。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomIdDto {

    /** 当前对外直播间编号 */
    private Long roomNo;

    /** 新的对外直播间编号 */
    private Long newRoomNo;
}
