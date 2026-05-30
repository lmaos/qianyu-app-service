package com.clmcat.qianyu.mall.ads.service;

import com.clmcat.qianyu.mall.ads.model.dto.RegionQueryDTO;
import com.clmcat.qianyu.mall.ads.model.entity.AdsRegion;
import com.clmcat.qianyu.mall.ads.model.vo.RegionNodeVO;
import com.clmcat.qianyu.mall.ads.support.AdsSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdsRegionViewBiz {

    @Resource
    private AdsRegionServiceBiz regionServiceBiz;

    public List<RegionNodeVO> getRegionList(RegionQueryDTO dto) {
        Long parentId = (dto == null || dto.getParentId() == null) ? 0L : dto.getParentId();
        List<AdsRegion> regions = regionServiceBiz.selectByParentId(parentId);
        return AdsSupport.toRegionNodeVOList(regions);
    }
}
