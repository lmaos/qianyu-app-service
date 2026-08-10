package com.clmcat.qianyu.live.api.room.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 开播返回结果。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveStartResult implements Serializable {

    /** 对外直播间编号 */
    private Long roomNo;

    /** 推流协议类型（服务端控制） */
    private PushType pushType;

    /** 推流地址 */
    private String pushUrl;

    /** 开播时间戳（毫秒） */
    private Long startTime;
}
