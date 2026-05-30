package com.clmcat.qianyu.mall.api.pms;

import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuDto;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface PmsSpuApi {

    PmsSpuDto getById(Long spuId);

    List<PmsSpuDto> batchGetByIds(Collection<Long> spuIds);

    void updateStatFields(Long spuId, BigDecimal minPrice, Integer sales, Integer commentCount, BigDecimal avgScore);
}
