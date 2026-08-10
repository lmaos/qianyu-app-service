package com.clmcat.qianyu.live.room.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建直播间参数（Controller 层参数对象）。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomCreateParamDto {

    /** 直播间标题 */
    private String title;

    /** 封面图 URL */
    private String coverImage;
}
