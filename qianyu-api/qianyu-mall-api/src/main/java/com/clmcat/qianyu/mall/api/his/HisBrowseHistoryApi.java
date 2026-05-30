package com.clmcat.qianyu.mall.api.his;

import com.clmcat.qianyu.mall.api.his.model.dto.HisBrowseHistoryDto;

import java.util.List;

/**
 * 浏览历史 RPC 接口
 */
public interface HisBrowseHistoryApi {

    /**
     * 查询用户浏览历史（推荐系统调用）
     */
    List<HisBrowseHistoryDto> getByUserId(Long userId, Integer limit);
}
