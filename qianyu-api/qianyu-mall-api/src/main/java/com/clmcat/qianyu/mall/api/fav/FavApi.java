package com.clmcat.qianyu.mall.api.fav;

/**
 * 收藏 RPC 接口
 */
public interface FavApi {

    /**
     * 检查用户是否已收藏指定目标
     */
    boolean isFavored(Long userId, Long targetId, Integer type);
}
