package com.clmcat.qianyu.mall.api.ads;

import com.clmcat.qianyu.mall.api.ads.model.dto.AdsAddressDto;

import java.util.List;

public interface AdsAddressApi {

    List<AdsAddressDto> getByUserId(Long userId);

    AdsAddressDto getById(Long addressId);
}
