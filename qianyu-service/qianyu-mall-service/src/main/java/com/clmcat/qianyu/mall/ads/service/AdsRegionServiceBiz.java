package com.clmcat.qianyu.mall.ads.service;

import com.clmcat.qianyu.mall.ads.mapper.AdsRegionMapper;
import com.clmcat.qianyu.mall.ads.model.entity.AdsRegion;
import com.clmcat.qianyu.mall.api.ads.AdsRegionApi;
import com.clmcat.qianyu.mall.api.ads.model.dto.AdsRegionDto;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@DubboService
@Service
public class AdsRegionServiceBiz implements AdsRegionApi {

    @Resource
    private AdsRegionMapper regionMapper;

    @Override
    public AdsRegionDto getByCode(String code) {
        if (code == null || code.isEmpty()) return null;
        com.mybatisflex.core.query.QueryWrapper qw = com.mybatisflex.core.query.QueryWrapper.create()
                .eq(AdsRegion::getCode, code);
        AdsRegion region = regionMapper.selectOneByQuery(qw);
        return toDto(region);
    }

    @Override
    public List<AdsRegionDto> getByParentId(Long parentId) {
        List<AdsRegion> regions = regionMapper.selectByParentId(parentId);
        List<AdsRegionDto> dtos = new ArrayList<>();
        if (regions != null) {
            for (AdsRegion r : regions) {
                AdsRegionDto dto = toDto(r);
                if (dto != null) dtos.add(dto);
            }
        }
        return dtos;
    }

    @Override
    public AdsRegionDto getById(Long id) {
        AdsRegion region = regionMapper.selectByRegionId(id);
        return toDto(region);
    }

    // ==================== Internal methods for ViewBiz ====================

    public List<AdsRegion> selectByParentId(Long parentId) {
        return regionMapper.selectByParentId(parentId);
    }

    public AdsRegion selectByRegionId(Long regionId) {
        return regionMapper.selectByRegionId(regionId);
    }

    private AdsRegionDto toDto(AdsRegion region) {
        if (region == null) return null;
        AdsRegionDto dto = new AdsRegionDto();
        dto.setId(region.getId());
        dto.setParentId(region.getParentId());
        dto.setName(region.getName());
        dto.setLevel(region.getLevel());
        dto.setCode(region.getCode());
        return dto;
    }
}
