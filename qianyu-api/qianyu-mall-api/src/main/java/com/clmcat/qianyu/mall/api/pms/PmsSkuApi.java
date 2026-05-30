package com.clmcat.qianyu.mall.api.pms;

import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSkuDto;

import java.util.List;

public interface PmsSkuApi {

    PmsSkuDto getById(Long skuId);

    List<PmsSkuDto> listBySpuId(Long spuId);
}
