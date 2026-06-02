package com.clmcat.qianyu.mall.ads.service;

import com.clmcat.qianyu.mall.ads.model.dto.AddressCreateDTO;
import com.clmcat.qianyu.mall.ads.model.dto.AddressUpdateDTO;
import com.clmcat.qianyu.mall.ads.model.vo.AddressItemVO;
import java.util.List;

public interface AdsAddressViewBiz {

    List<AddressItemVO> getAddressList(long userId);

    AddressItemVO getAddressDetail(long userId, Long addressId);

    Long createAddress(long userId, AddressCreateDTO dto);

    void updateAddress(long userId, AddressUpdateDTO dto);

    void deleteAddress(long userId, Long addressId);

    void setDefault(long userId, Long addressId);

}