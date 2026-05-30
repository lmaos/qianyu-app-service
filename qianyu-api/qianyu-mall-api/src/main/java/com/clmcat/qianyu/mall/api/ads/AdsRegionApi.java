package com.clmcat.qianyu.mall.api.ads;

import com.clmcat.qianyu.mall.api.ads.model.dto.AdsRegionDto;

import java.util.List;

public interface AdsRegionApi {

    AdsRegionDto getByCode(String code);

    List<AdsRegionDto> getByParentId(Long parentId);

    AdsRegionDto getById(Long id);
}
