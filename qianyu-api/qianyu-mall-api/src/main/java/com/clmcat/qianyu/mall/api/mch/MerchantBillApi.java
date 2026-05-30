package com.clmcat.qianyu.mall.api.mch;

import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantAccountDto;

public interface MerchantBillApi {

    MerchantAccountDto getByMerchantId(Long merchantId);
}
