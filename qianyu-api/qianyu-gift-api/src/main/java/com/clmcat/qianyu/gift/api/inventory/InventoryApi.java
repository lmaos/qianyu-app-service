package com.clmcat.qianyu.gift.api.inventory;

import com.clmcat.qianyu.gift.api.inventory.model.dto.InventoryListDto;

/**
 * 背包 RPC 接口。
 * <p>
 * 提供礼物库存的查询和管理能力。
 *
 * @author ark-home
 * @date 2026-08-07
 */
public interface InventoryApi {

    /**
     * 背包礼物列表（游标分页，按过期时间升序）。
     *
     * @param userId 用户ID
     * @param cursor 游标（上一页最后一条的ID，首次传0）
     * @param limit  每页数量（最大50）
     * @return 背包礼物列表
     */
    InventoryListDto getGiftInventory(long userId, long cursor, int limit);

    /**
     * 增加礼物到库存（活动/Task 发奖调用）。
     * <p>
     * 相同用户+相同礼物+相同过期日期自动聚合累加。
     *
     * @param userId     用户ID
     * @param giftId     礼物ID
     * @param quantity   数量
     * @param expireTime 过期时间戳（毫秒），0=永久
     * @param sourceType 来源类型
     * @param sourceId   来源单号
     */
    void addGiftToInventory(long userId, long giftId, int quantity, long expireTime,
                            String sourceType, String sourceId);
}
