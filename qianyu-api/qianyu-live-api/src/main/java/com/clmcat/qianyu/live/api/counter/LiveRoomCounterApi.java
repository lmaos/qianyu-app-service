package com.clmcat.qianyu.live.api.counter;

/**
 * 直播间计数器 RPC 接口。
 * <p>
 * 提供直播间计数器的原子累加操作，供礼物模块、评论模块等跨模块调用。
 *
 * @author ark-home
 * @date 2026-08-07
 */
public interface LiveRoomCounterApi {

    /**
     * 原子累加礼物计数（gift_count + count, gift_amount + amount）。
     *
     * @param roomId 直播间ID（live_room.id）
     * @param count  礼物数量增量
     * @param amount 礼物金额增量（最小单位）
     * @return true=成功, false=直播间计数器不存在
     */
    boolean incrementGiftCount(long roomId, long count, long amount);
}
