package com.clmcat.qianyu.mall.api.pms;

import com.clmcat.qianyu.mall.api.pms.model.dto.PmsCategoryDto;

public interface PmsCategoryApi {

    PmsCategoryDto getById(Long categoryId);
}
