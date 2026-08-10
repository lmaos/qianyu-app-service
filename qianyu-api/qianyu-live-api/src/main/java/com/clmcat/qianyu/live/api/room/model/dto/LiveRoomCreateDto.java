package com.clmcat.qianyu.live.api.room.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建/更新直播间参数（RPC 入参）。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomCreateDto implements Serializable {

    /** 直播间标题 */
    private String title;

    /** 封面图 URL */
    private String coverImage;
}
