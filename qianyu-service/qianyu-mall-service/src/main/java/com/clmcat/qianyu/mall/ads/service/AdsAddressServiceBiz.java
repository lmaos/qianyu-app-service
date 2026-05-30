package com.clmcat.qianyu.mall.ads.service;

import com.clmcat.qianyu.mall.ads.mapper.AdsAddressMapper;
import com.clmcat.qianyu.mall.ads.model.entity.AdsAddress;
import com.clmcat.qianyu.mall.api.ads.AdsAddressApi;
import com.clmcat.qianyu.mall.api.ads.model.dto.AdsAddressDto;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@DubboService
@Service
public class AdsAddressServiceBiz implements AdsAddressApi {

    @Resource
    private AdsAddressMapper addressMapper;

    @Override
    public List<AdsAddressDto> getByUserId(Long userId) {
        List<AdsAddress> addresses = addressMapper.selectByUserId(userId);
        List<AdsAddressDto> dtos = new ArrayList<>();
        if (addresses != null) {
            for (AdsAddress a : addresses) {
                AdsAddressDto dto = toDto(a);
                if (dto != null) dtos.add(dto);
            }
        }
        return dtos;
    }

    @Override
    public AdsAddressDto getById(Long addressId) {
        AdsAddress address = addressMapper.selectOneById(addressId);
        return toDto(address);
    }

    // ==================== Internal methods for ViewBiz ====================

    public List<AdsAddress> selectByUserId(long userId) {
        return addressMapper.selectByUserId(userId);
    }

    public AdsAddress selectOneById(Long addressId) {
        return addressMapper.selectOneById(addressId);
    }

    public int countByUserId(long userId) {
        return addressMapper.countByUserId(userId);
    }

    public void clearDefault(long userId) {
        addressMapper.clearDefault(userId);
    }

    public void insertSelective(AdsAddress address) {
        addressMapper.insertSelective(address);
    }

    public void updateAddress(AdsAddress address) {
        addressMapper.update(address);
    }

    public void deleteById(Long addressId) {
        addressMapper.deleteById(addressId);
    }

    private AdsAddressDto toDto(AdsAddress address) {
        if (address == null) return null;
        AdsAddressDto dto = new AdsAddressDto();
        dto.setId(address.getId());
        dto.setUserId(address.getUserId());
        dto.setName(address.getName());
        dto.setPhone(address.getPhone());
        dto.setProvince(address.getProvince());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setDetail(address.getDetail());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }
}
