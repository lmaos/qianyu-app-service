package com.clmcat.qianyu.mall.api.mch;

import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;

public interface MerchantApi {

    MerchantDto getByUserId(Long userId);

    MerchantDto getById(Long merchantId);
}
