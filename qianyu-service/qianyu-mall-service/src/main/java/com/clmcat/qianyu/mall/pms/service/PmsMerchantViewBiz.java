package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.pms.model.dto.MerchantGoodsQueryDTO;
import com.clmcat.qianyu.mall.pms.model.dto.SkuBatchUpdateDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuCreateDto;
import com.clmcat.qianyu.mall.pms.model.dto.SpuUpdateDto;
import com.clmcat.qianyu.mall.pms.model.vo.MerchantGoodsPageVO;

public interface PmsMerchantViewBiz {

    MerchantGoodsPageVO getGoodsPage(long userId, MerchantGoodsQueryDTO dto);

    Long createSpu(long userId, SpuCreateDto dto);

    void updateSpu(long userId, SpuUpdateDto dto);

    void listOnSpu(long userId, Long spuId);

    void listOffSpu(long userId, Long spuId);

    void skuBatchUpdate(long userId, SkuBatchUpdateDto dto);

}