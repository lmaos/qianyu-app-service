package com.clmcat.qianyu.mall.api.his;

import com.clmcat.qianyu.mall.api.his.model.dto.HisSearchKeywordDto;

import java.util.List;

/**
 * 搜索热词 RPC 接口
 */
public interface HisSearchKeywordApi {

    /**
     * 获取热词列表
     */
    List<HisSearchKeywordDto> getHotKeywords(Integer limit);
}
