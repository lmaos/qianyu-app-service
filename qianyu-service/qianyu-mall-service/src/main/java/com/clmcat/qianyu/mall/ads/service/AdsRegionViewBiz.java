package com.clmcat.qianyu.mall.ads.service;

import com.clmcat.qianyu.mall.ads.model.dto.RegionQueryDTO;
import com.clmcat.qianyu.mall.ads.model.vo.RegionNodeVO;
import java.util.List;

public interface AdsRegionViewBiz {

    List<RegionNodeVO> getRegionList(RegionQueryDTO dto);

}