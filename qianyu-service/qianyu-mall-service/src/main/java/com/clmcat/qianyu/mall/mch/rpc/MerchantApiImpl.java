package com.clmcat.qianyu.mall.mch.rpc;

import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.mch.mapper.MerchantMapper;
import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@DubboService
@Service
public class MerchantApiImpl implements MerchantApi {

    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private com.clmcat.qianyu.mall.mch.mapper.MerchantStoreMapper merchantStoreMapper;

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

    @Override
    public java.util.List<MerchantDto> pageMerchants(com.clmcat.qianyu.mall.api.mch.model.dto.MerchantPageQueryDTO query) {
        com.mybatisflex.core.query.QueryWrapper qw = com.mybatisflex.core.query.QueryWrapper.create();
        if (query.getAuditStatus() != null) qw.and("audit_status = ?", query.getAuditStatus());
        if (query.getStatus() != null) qw.and("status = ?", query.getStatus());
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) qw.and("name like ?", "%" + query.getKeyword() + "%");
        qw.orderBy("create_time DESC");
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;
        com.mybatisflex.core.paginate.Page<Merchant> page = merchantMapper.paginate(
                com.mybatisflex.core.paginate.Page.of(pageNum, pageSize), qw);
        return page.getRecords().stream().map(this::toDto).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void updateMerchantStatus(Long merchantId, Integer status) {
        Merchant merchant = merchantMapper.selectOneById(merchantId);
        com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus.MCH_MERCHANT_NOT_FOUND.assertThrowResEx(merchant == null);
        merchant.setStatus(status);
        merchant.setUpdateTime(System.currentTimeMillis());
        merchantMapper.update(merchant);
    }

    @Override
    public void auditMerchant(Long merchantId, Boolean approved, String rejectReason) {
        com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus.MCH_MERCHANT_NOT_FOUND.assertThrowResEx(
                com.clmcat.qianyu.mall.mch.support.MerchantConvert.isNullOrNonPositive(merchantId));
        Merchant merchant = merchantMapper.selectOneById(merchantId);
        com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus.MCH_MERCHANT_NOT_FOUND.assertThrowResEx(merchant == null);
        long now = System.currentTimeMillis();
        if (Boolean.TRUE.equals(approved)) {
            merchant.setAuditStatus(1);
            merchant.setStatus(1);
            merchant.setUpdateTime(now);
            merchantMapper.update(merchant);
            com.clmcat.qianyu.mall.mch.model.entity.MerchantStore store = merchantStoreMapper.selectOneByQuery(
                    com.mybatisflex.core.query.QueryWrapper.create().where("merchant_id = ?", merchantId));
            if (store != null) {
                store.setStatus(1);
                store.setUpdateTime(now);
                merchantStoreMapper.update(store);
            }
        } else {
            merchant.setAuditStatus(2);
            merchant.setAuditRemark(rejectReason);
            merchant.setUpdateTime(now);
            merchantMapper.update(merchant);
        }
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
