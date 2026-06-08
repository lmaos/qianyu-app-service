package com.clmcat.qianyu.mall.cms.service;

import com.clmcat.qianyu.mall.cms.model.vo.HomePageVo;
import com.clmcat.qianyu.mall.cms.model.vo.TabZoneListVo;

public interface CmsViewBiz {

    HomePageVo getHomePage();

    /**
     * 单个 Tab 下的 Zone 楼层列表
     *
     * @param categoryId Tab 关联的分类 ID；传 0 / null 时返回 recommend 默认的 zoneList
     */
    TabZoneListVo getTabZoneList(Long categoryId);

}