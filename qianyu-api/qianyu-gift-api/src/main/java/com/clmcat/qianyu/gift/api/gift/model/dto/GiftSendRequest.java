package com.clmcat.qianyu.gift.api.gift.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 送礼请求 DTO。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftSendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 送礼人用户ID */
    private Long senderUserId;

    /** 收礼人用户ID（主播） */
    private Long receiverUserId;

    /** 礼物ID */
    private Long giftId;

    /** 赠送数量（盲盒/奖池恒为1） */
    private Integer quantity;

    /** 场景类型：live_room/voice_room/private_chat */
    private String sceneType;

    /** 直播间ID（直播间场景必填） */
    private Long roomId;

    /** 支付方式：1=虚拟币 2=背包礼物（暂不支持） */
    private Integer payType;

    /** 幂等键（全局唯一，客户端生成） */
    private String idempotentKey;
}
