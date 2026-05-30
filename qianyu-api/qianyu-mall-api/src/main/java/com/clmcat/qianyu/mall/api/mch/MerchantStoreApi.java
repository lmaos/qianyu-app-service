package com.clmcat.qianyu.mall.api.mch;

import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantStoreDto;

import java.util.Collection;
import java.util.List;

public interface MerchantStoreApi {

    MerchantStoreDto getByMerchantId(Long merchantId);

    MerchantStoreDto getById(Long storeId);

    List<MerchantStoreDto> batchGetByMerchantIds(Collection<Long> merchantIds);
}
