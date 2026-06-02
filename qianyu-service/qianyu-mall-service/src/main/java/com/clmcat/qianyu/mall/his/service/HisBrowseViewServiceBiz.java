package com.clmcat.qianyu.mall.his.service;

import com.clmcat.qianyu.mall.his.model.dto.BrowseHistoryDeleteDTO;
import com.clmcat.qianyu.mall.his.model.dto.BrowseHistoryQueryDTO;
import com.clmcat.qianyu.mall.his.model.dto.BrowseRecordDTO;
import com.clmcat.qianyu.mall.his.model.vo.BrowseHistoryDeleteResultVO;
import com.clmcat.qianyu.mall.his.model.vo.BrowseHistoryItemVO;
import com.mybatisflex.core.paginate.Page;

public interface HisBrowseViewServiceBiz {

    Page<BrowseHistoryItemVO> getBrowseHistoryList(long userId, BrowseHistoryQueryDTO dto);

    void recordBrowse(long userId, BrowseRecordDTO dto);

    BrowseHistoryDeleteResultVO deleteBrowseHistory(long userId, BrowseHistoryDeleteDTO dto);

}