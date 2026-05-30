package com.clmcat.qianyu.mall.api.mch;

import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantAccountDto;

public interface MerchantAccountApi {

    MerchantAccountDto getByMerchantId(Long merchantId);

    MerchantAccountDto getById(Long accountId);
}
