package com.clmcat.qianyu.gift.api.shelf;

import com.clmcat.qianyu.gift.api.shelf.model.dto.ShelfConfigDto;

/**
 * 礼物架 RPC 接口。
 *
 * @author ark-home
 * @date 2026-08-07
 */
public interface GiftShelfApi {

    /**
     * 获取场景的礼物架配置。
     *
     * @param sceneType 场景类型：live_room/voice_room/private_chat
     * @return 礼物架配置（按 category 分组）
     */
    ShelfConfigDto getShelfConfig(String sceneType);
}
