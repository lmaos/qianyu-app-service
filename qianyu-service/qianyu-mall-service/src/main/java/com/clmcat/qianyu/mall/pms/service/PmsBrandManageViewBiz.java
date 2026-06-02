package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.pms.model.dto.BrandCreateDto;
import com.clmcat.qianyu.mall.pms.model.dto.BrandUpdateDto;

public interface PmsBrandManageViewBiz {

    Long createBrand(BrandCreateDto dto);

    void updateBrand(BrandUpdateDto dto);

    void deleteBrand(Long brandId);

}