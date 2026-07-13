package com.clmcat.qianyu.mall.api.pms;

import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSkuDto;

import java.util.List;

public interface PmsSkuApi {

    PmsSkuDto getById(Long skuId);

    List<PmsSkuDto> listBySpuId(Long spuId);

    /** S16 新增：按商家 ID 查其全部 SKU，供库存/物流域做商家归属过滤。 */
    List<PmsSkuDto> listByMerchantId(Long merchantId);
}
