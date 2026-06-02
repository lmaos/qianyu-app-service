package com.clmcat.qianyu.mall.his.service;

import com.clmcat.qianyu.mall.his.model.dto.SearchHotQueryDTO;
import com.clmcat.qianyu.mall.his.model.dto.SearchKeywordRecordDTO;
import com.clmcat.qianyu.mall.his.model.vo.HotKeywordVO;
import java.util.List;

public interface HisSearchViewServiceBiz {

    List<HotKeywordVO> getHotKeywords(SearchHotQueryDTO dto);

    void recordKeyword(SearchKeywordRecordDTO dto);

}