package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.api.pms.PmsSkuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSkuDto;
import com.clmcat.qianyu.mall.pms.mapper.PmsSkuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSku;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.clmcat.qianyu.mall.pms.model.entity.table.PmsSkuTableDef.PMS_SKU;

@DubboService
@Service
public class PmsSkuServiceBiz implements PmsSkuApi {

    @Resource
    private PmsSkuMapper skuMapper;

    @Override
    public PmsSkuDto getById(Long skuId) {
        if (skuId == null || skuId <= 0) {
            return null;
        }
        PmsSku sku = skuMapper.selectOneByQuery(
                QueryWrapper.create().where(PMS_SKU.ID.eq(skuId))
                        .and(PMS_SKU.DELETED.eq(0)));
        return toDto(sku);
    }

    @Override
    public List<PmsSkuDto> listBySpuId(Long spuId) {
        if (spuId == null || spuId <= 0) {
            return new ArrayList<>();
        }
        List<PmsSku> skuList = skuMapper.selectBySpuId(spuId);
        List<PmsSkuDto> result = new ArrayList<>();
        for (PmsSku sku : skuList) {
            PmsSkuDto dto = toDto(sku);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    // ==================== Internal methods for ViewBiz ====================

    public java.util.List<PmsSku> selectBySpuId(Long spuId) {
        return skuMapper.selectBySpuId(spuId);
    }

    public PmsSku selectOneById(Long id) {
        return skuMapper.selectOneById(id);
    }

    public void insertSelective(PmsSku sku) {
        skuMapper.insertSelective(sku);
    }

    public void updateSku(PmsSku sku) {
        skuMapper.update(sku);
    }

    public void clearDefault(Long spuId, long updateTime) {
        skuMapper.clearDefault(spuId, updateTime);
    }

    private PmsSkuDto toDto(PmsSku entity) {
        if (entity == null) {
            return null;
        }
        PmsSkuDto dto = new PmsSkuDto();
        dto.setId(entity.getId());
        dto.setMerchantId(entity.getMerchantId());
        dto.setSpuId(entity.getSpuId());
        dto.setSkuCode(entity.getSkuCode());
        dto.setBarcode(entity.getBarcode());
        dto.setAttributes(entity.getAttributes());
        dto.setSkuName(entity.getSkuName());
        dto.setSkuImage(entity.getSkuImage());
        dto.setPrice(entity.getPrice());
        dto.setOriginalPrice(entity.getOriginalPrice());
        dto.setCostPrice(entity.getCostPrice());
        dto.setStatus(entity.getStatus());
        dto.setIsDefault(entity.getIsDefault());
        dto.setWeight(entity.getWeight());
        dto.setVolume(entity.getVolume());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }
}
