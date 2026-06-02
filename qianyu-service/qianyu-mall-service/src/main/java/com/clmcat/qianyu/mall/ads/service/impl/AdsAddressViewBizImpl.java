package com.clmcat.qianyu.mall.ads.service.impl;

import com.clmcat.qianyu.mall.ads.rpc.AdsAddressApiImpl;
import com.clmcat.qianyu.mall.ads.rpc.AdsRegionApiImpl;
import com.clmcat.qianyu.mall.ads.model.dto.AddressCreateDTO;
import com.clmcat.qianyu.mall.ads.model.dto.AddressUpdateDTO;
import com.clmcat.qianyu.mall.ads.model.entity.AdsAddress;
import com.clmcat.qianyu.mall.ads.model.entity.AdsRegion;
import com.clmcat.qianyu.mall.ads.model.entity.status.AdsStatus;
import com.clmcat.qianyu.mall.ads.model.vo.AddressItemVO;
import com.clmcat.qianyu.mall.ads.support.AdsSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import com.clmcat.qianyu.mall.ads.service.AdsAddressViewBiz;

@Service
public class AdsAddressViewBizImpl implements AdsAddressViewBiz {

    @Resource
    private AdsAddressApiImpl addressServiceBiz;

    @Resource
    private AdsRegionApiImpl regionServiceBiz;

    public List<AddressItemVO> getAddressList(long userId) {
        List<AdsAddress> addresses = addressServiceBiz.selectByUserId(userId);
        return AdsSupport.toAddressItemVOList(addresses);
    }

    public AddressItemVO getAddressDetail(long userId, Long addressId) {
        AdsStatus.ADS_ADDRESS_NOT_FOUND.assertThrowResEx(AdsSupport.isNullOrNonPositive(addressId));
        AdsAddress address = addressServiceBiz.selectOneById(addressId);
        AdsStatus.ADS_ADDRESS_NOT_FOUND.assertThrowResEx(address == null);
        AdsStatus.ADS_ADDRESS_NOT_BELONG_USER.assertThrowResEx(!address.getUserId().equals(userId));
        return AdsSupport.toAddressItemVO(address);
    }

    public Long createAddress(long userId, AddressCreateDTO dto) {
        AdsStatus.ADS_DETAIL_REQUIRED.assertThrowResEx(dto.getDetail() == null || dto.getDetail().trim().isEmpty());
        int count = addressServiceBiz.countByUserId(userId);
        AdsStatus.ADS_ADDRESS_LIMIT_EXCEED.assertThrowResEx(count >= 20);

        AdsAddress address = new AdsAddress();
        long addressId = AdsSupport.ADDRESS_ID_SNOWFLAKE.nextId();
        address.setId(addressId);
        address.setUserId(userId);
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setDetail(dto.getDetail());
        address.setProvince(dto.getProvinceCode() != null ? dto.getProvinceCode() : "");
        address.setCity(dto.getCityCode() != null ? dto.getCityCode() : "");
        address.setDistrict(dto.getDistrictCode() != null ? dto.getDistrictCode() : "");
        address.setProvinceCode(dto.getProvinceCode());
        address.setCityCode(dto.getCityCode());
        address.setDistrictCode(dto.getDistrictCode());
        address.setTag(dto.getTag());
        address.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault() ? 1 : 0);
        address.setCreateTime(System.currentTimeMillis());
        address.setUpdateTime(System.currentTimeMillis());
        address.setDeleted(0);

        // Resolve region names
        resolveRegionNames(address);

        // Clear old default if setting new default
        if (address.getIsDefault() == 1) {
            addressServiceBiz.clearDefault(userId);
        }

        addressServiceBiz.insertSelective(address);
        return addressId;
    }

    public void updateAddress(long userId, AddressUpdateDTO dto) {
        AdsStatus.ADS_ADDRESS_NOT_FOUND.assertThrowResEx(dto == null || AdsSupport.isNullOrNonPositive(dto.getAddressId()));
        AdsAddress address = addressServiceBiz.selectOneById(dto.getAddressId());
        AdsStatus.ADS_ADDRESS_NOT_FOUND.assertThrowResEx(address == null);
        AdsStatus.ADS_ADDRESS_NOT_BELONG_USER.assertThrowResEx(!address.getUserId().equals(userId));

        if (dto.getName() != null) address.setName(dto.getName());
        if (dto.getPhone() != null) address.setPhone(dto.getPhone());
        if (dto.getDetail() != null) address.setDetail(dto.getDetail());
        if (dto.getProvinceCode() != null) address.setProvinceCode(dto.getProvinceCode());
        if (dto.getCityCode() != null) address.setCityCode(dto.getCityCode());
        if (dto.getDistrictCode() != null) address.setDistrictCode(dto.getDistrictCode());
        if (dto.getTag() != null) address.setTag(dto.getTag());

        resolveRegionNames(address);

        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            addressServiceBiz.clearDefault(userId);
            address.setIsDefault(1);
        }

        address.setUpdateTime(System.currentTimeMillis());
        addressServiceBiz.updateAddress(address);
    }

    public void deleteAddress(long userId, Long addressId) {
        AdsStatus.ADS_ADDRESS_NOT_FOUND.assertThrowResEx(AdsSupport.isNullOrNonPositive(addressId));
        AdsAddress address = addressServiceBiz.selectOneById(addressId);
        AdsStatus.ADS_ADDRESS_NOT_FOUND.assertThrowResEx(address == null);
        AdsStatus.ADS_ADDRESS_NOT_BELONG_USER.assertThrowResEx(!address.getUserId().equals(userId));
        AdsStatus.ADS_CANNOT_DELETE_DEFAULT.assertThrowResEx(address.getIsDefault() != null && address.getIsDefault() == 1);

        addressServiceBiz.deleteById(addressId);
    }

    public void setDefault(long userId, Long addressId) {
        AdsStatus.ADS_ADDRESS_NOT_FOUND.assertThrowResEx(AdsSupport.isNullOrNonPositive(addressId));
        AdsAddress address = addressServiceBiz.selectOneById(addressId);
        AdsStatus.ADS_ADDRESS_NOT_FOUND.assertThrowResEx(address == null);
        AdsStatus.ADS_ADDRESS_NOT_BELONG_USER.assertThrowResEx(!address.getUserId().equals(userId));

        addressServiceBiz.clearDefault(userId);
        address.setIsDefault(1);
        address.setUpdateTime(System.currentTimeMillis());
        addressServiceBiz.updateAddress(address);
    }

    private void resolveRegionNames(AdsAddress address) {
        if (address.getProvinceCode() != null) {
            AdsRegion province = regionServiceBiz.selectByRegionId(Long.parseLong(address.getProvinceCode()));
            if (province != null) address.setProvince(province.getName());
        }
        if (address.getCityCode() != null) {
            AdsRegion city = regionServiceBiz.selectByRegionId(Long.parseLong(address.getCityCode()));
            if (city != null) address.setCity(city.getName());
        }
        if (address.getDistrictCode() != null) {
            AdsRegion district = regionServiceBiz.selectByRegionId(Long.parseLong(address.getDistrictCode()));
            if (district != null) address.setDistrict(district.getName());
        }
    }
}
