package com.clmcat.qianyu.mall.mch.service;

import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.mch.mapper.MerchantMapper;
import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@DubboService
@Service
public class MerchantServiceBiz implements MerchantApi {

    @Resource
    private MerchantMapper merchantMapper;

    @Override
    public MerchantDto getByUserId(Long userId) {
        Merchant merchant = merchantMapper.selectByUserId(userId);
        return toDto(merchant);
    }

    @Override
    public MerchantDto getById(Long merchantId) {
        Merchant merchant = merchantMapper.selectOneById(merchantId);
        return toDto(merchant);
    }

    // ==================== Internal methods for ViewBiz ====================

    public Merchant selectOneById(Long id) {
        return merchantMapper.selectOneById(id);
    }

    public Merchant selectByUserId(long userId) {
        return merchantMapper.selectByUserId(userId);
    }

    public void updateMerchant(Merchant merchant) {
        merchantMapper.update(merchant);
    }

    public void insertSelective(Merchant merchant) {
        merchantMapper.insertSelective(merchant);
    }

    private MerchantDto toDto(Merchant merchant) {
        if (merchant == null) return null;
        MerchantDto dto = new MerchantDto();
        dto.setId(merchant.getId());
        dto.setUserId(merchant.getUserId());
        dto.setName(merchant.getName());
        dto.setType(merchant.getType());
        dto.setContactName(merchant.getContactName());
        dto.setContactPhone(merchant.getContactPhone());
        dto.setLicenseNo(merchant.getLicenseNo());
        dto.setLicenseImage(merchant.getLicenseImage());
        dto.setDescription(merchant.getDescription());
        dto.setBankName(merchant.getBankName());
        dto.setBankAccount(merchant.getBankAccount());
        dto.setBankHolder(merchant.getBankHolder());
        dto.setSettlementCycle(merchant.getSettlementCycle());
        dto.setAuditStatus(merchant.getAuditStatus());
        dto.setAuditRemark(merchant.getAuditRemark());
        dto.setStatus(merchant.getStatus());
        dto.setCreateTime(merchant.getCreateTime());
        dto.setUpdateTime(merchant.getUpdateTime());
        return dto;
    }
}
