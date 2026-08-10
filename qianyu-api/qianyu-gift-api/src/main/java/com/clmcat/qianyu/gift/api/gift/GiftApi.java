package com.clmcat.qianyu.gift.api.gift;

import com.clmcat.qianyu.gift.api.gift.model.dto.GiftDto;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendRequest;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendResult;

import java.util.List;

/**
 * 礼物 RPC 接口。
 * <p>
 * 提供送礼、礼物查询等核心能力。送礼通过 {@code TradeServiceBiz} 完成扣款和结算。
 *
 * @author ark-home
 * @date 2026-08-07
 */
public interface GiftApi {

    /**
     * 送礼。
     * <p>
     * 幂等：同一 idempotentKey 重放返回已有结果，不重复扣款。
     *
     * @param req 送礼请求（senderUserId, receiverUserId, giftId, quantity, sceneType, roomId, idempotentKey）
     * @return 送礼结果（含送礼记录ID、交易ID、结算金额等）
     */
    GiftSendResult sendGift(GiftSendRequest req);

    /**
     * 查询单个礼物配置。
     *
     * @param giftId 礼物ID
     * @return 礼物信息，不存在返回 null
     */
    GiftDto getGift(long giftId);

    /**
     * 批量查询礼物（用于礼物架渲染）。
     *
     * @param giftIds 礼物ID列表
     * @return 礼物信息列表
     */
    List<GiftDto> batchGetGifts(List<Long> giftIds);
}
