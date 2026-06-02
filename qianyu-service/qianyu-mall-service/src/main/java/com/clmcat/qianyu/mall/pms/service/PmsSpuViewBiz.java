package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.pms.model.dto.SpuSearchDto;
import com.clmcat.qianyu.mall.pms.model.vo.SpuDetailVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpuSimpleVo;
import com.mybatisflex.core.paginate.Page;

public interface PmsSpuViewBiz {

    Page<SpuSimpleVo> searchSpu(SpuSearchDto dto);

    SpuDetailVo getSpuDetail(Long spuId);

    SpuDetailVo getSpuDetailBySku(Long skuId);

}