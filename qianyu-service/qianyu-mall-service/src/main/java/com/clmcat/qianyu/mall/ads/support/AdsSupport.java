package com.clmcat.qianyu.mall.ads.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.mall.ads.model.entity.AdsAddress;
import com.clmcat.qianyu.mall.ads.model.entity.AdsRegion;
import com.clmcat.qianyu.mall.ads.model.vo.AddressItemVO;
import com.clmcat.qianyu.mall.ads.model.vo.RegionNodeVO;

import java.util.ArrayList;
import java.util.List;

public class AdsSupport {

    public static final CustomSnowflake ADDRESS_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    public static RegionNodeVO toRegionNodeVO(AdsRegion region) {
        if (region == null) return null;
        return RegionNodeVO.builder()
                .id(region.getId())
                .name(region.getName())
                .parentId(region.getParentId())
                .level(region.getLevel())
                .build();
    }

    public static List<RegionNodeVO> toRegionNodeVOList(List<AdsRegion> regions) {
        List<RegionNodeVO> list = new ArrayList<>();
        if (regions == null) return list;
        for (AdsRegion region : regions) {
            RegionNodeVO vo = toRegionNodeVO(region);
            if (vo != null) list.add(vo);
        }
        return list;
    }

    public static AddressItemVO toAddressItemVO(AdsAddress address) {
        if (address == null) return null;
        String fullAddress = (address.getProvince() != null ? address.getProvince() : "")
                + (address.getCity() != null ? address.getCity() : "")
                + (address.getDistrict() != null ? address.getDistrict() : "")
                + (address.getDetail() != null ? address.getDetail() : "");
        return AddressItemVO.builder()
                .id(address.getId())
                .name(address.getName())
                .phone(address.getPhone())
                .country(address.getCountry())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detail(address.getDetail())
                .isDefault(address.getIsDefault() != null && address.getIsDefault() == 1)
                .fullAddress(fullAddress)
                .build();
    }

    public static List<AddressItemVO> toAddressItemVOList(List<AdsAddress> addresses) {
        List<AddressItemVO> list = new ArrayList<>();
        if (addresses == null) return list;
        for (AdsAddress addr : addresses) {
            AddressItemVO vo = toAddressItemVO(addr);
            if (vo != null) list.add(vo);
        }
        return list;
    }

    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.doubleValue() <= 0;
    }
}
