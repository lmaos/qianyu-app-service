package com.clmcat.qianyu.mall.ads.service.impl;

import com.clmcat.qianyu.mall.ads.rpc.AdsRegionApiImpl;
import com.clmcat.qianyu.mall.ads.model.dto.RegionQueryDTO;
import com.clmcat.qianyu.mall.ads.model.entity.AdsRegion;
import com.clmcat.qianyu.mall.ads.model.vo.RegionNodeVO;
import com.clmcat.qianyu.mall.ads.support.AdsSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import com.clmcat.qianyu.mall.ads.service.AdsRegionViewBiz;

@Service
public class AdsRegionViewBizImpl implements AdsRegionViewBiz {

    @Resource
    private AdsRegionApiImpl regionServiceBiz;

    public List<RegionNodeVO> getRegionList(RegionQueryDTO dto) {
        Long parentId = (dto == null || dto.getParentId() == null) ? 0L : dto.getParentId();
        List<AdsRegion> regions = regionServiceBiz.selectByParentId(parentId);
        return AdsSupport.toRegionNodeVOList(regions);
    }
}
