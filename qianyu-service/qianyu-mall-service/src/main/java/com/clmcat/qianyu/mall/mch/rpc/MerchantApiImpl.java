package com.clmcat.qianyu.mall.mch.rpc;

import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.api.msg.MsgApi;
import com.clmcat.qianyu.mall.mch.mapper.MerchantMapper;
import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@Slf4j
@DubboService
@Service
public class MerchantApiImpl implements MerchantApi {

    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private com.clmcat.qianyu.mall.mch.mapper.MerchantStoreMapper merchantStoreMapper;
    /** 系统通知投递（审核结果通知；跨模块走 Dubbo）。 */
    @DubboReference
    private MsgApi msgApi;

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
    public MerchantDto requireActiveMerchant(Long userId) {
        com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(
                userId == null || userId <= 0);
        Merchant m = merchantMapper.selectByUserId(userId);
        com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(m == null);
        // 冻结优先判（status=2）
        if (Integer.valueOf(2).equals(m.getStatus())) {
            com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus.MCH_MERCHANT_FROZEN.assertThrowResEx(true);
        }
        // 必须 审核通过(audit=1) 且 生效(status=1)
        boolean active = Integer.valueOf(1).equals(m.getAuditStatus())
                && Integer.valueOf(1).equals(m.getStatus());
        com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus.MCH_MERCHANT_NOT_APPROVED.assertThrowResEx(!active);
        return toDto(m);
    }

    @Override
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<MerchantDto> pageMerchants(com.clmcat.qianyu.mall.api.mch.model.dto.MerchantPageQueryDTO query) {
        com.mybatisflex.core.query.QueryWrapper qw = com.mybatisflex.core.query.QueryWrapper.create();
        if (query.getAuditStatus() != null) qw.and("audit_status = ?", query.getAuditStatus());
        if (query.getStatus() != null) qw.and("status = ?", query.getStatus());
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) qw.and("name like ?", "%" + query.getKeyword() + "%");
        if (query.getStartTime() != null) qw.and("create_time >= ?", query.getStartTime());
        if (query.getEndTime() != null) qw.and("create_time <= ?", query.getEndTime());
        qw.orderBy("create_time DESC");
        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;
        com.mybatisflex.core.paginate.Page<Merchant> page = merchantMapper.paginate(
                com.mybatisflex.core.paginate.Page.of(pageNum, pageSize), qw);
        java.util.List<MerchantDto> records = page.getRecords().stream().map(this::toDto).collect(java.util.stream.Collectors.toList());
        return com.clmcat.qianyu.mall.api.model.dto.PageResultDTO.<MerchantDto>builder()
                .records(records).total(page.getTotalRow())
                .pageNum(page.getPageNumber()).pageSize(page.getPageSize()).build();
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
            sendSafely(merchant.getUserId(), 1, "入驻审核通过",
                    "您的商户资质已通过审核，现在可以上架商品了。", "merchant_audit", merchantId);
        } else {
            merchant.setAuditStatus(2);
            merchant.setAuditRemark(rejectReason);
            merchant.setUpdateTime(now);
            merchantMapper.update(merchant);
            sendSafely(merchant.getUserId(), 1, "入驻审核未通过",
                    "您的商户资质未通过：" + (rejectReason == null ? "" : rejectReason) + "。可修改后重新提交。",
                    "merchant_audit", merchantId);
        }
    }

    /** 投递通知（独立、失败降级，不阻断审核——决策 D-05）。 */
    private void sendSafely(Long userId, Integer type, String title, String content, String bizType, Long bizId) {
        try {
            msgApi.send(userId, type, title, content, bizType, bizId);
        } catch (Exception e) {
            log.warn("审核结果通知投递失败 userId={} bizId={}: {}", userId, bizId, e.getMessage());
        }
    }

    // ==================== Internal methods for ViewBiz ====================

    public Merchant selectOneById(Long id) {
        return merchantMapper.selectOneById(id);
    }

    public Merchant selectByUserId(long userId) {
        return merchantMapper.selectByUserId(userId);
    }

    public Merchant selectByLicenseNo(String licenseNo) {
        return merchantMapper.selectByLicenseNo(licenseNo);
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
        dto.setBankBranch(merchant.getBankBranch());
        dto.setLegalPersonName(merchant.getLegalPersonName());
        dto.setLegalPersonIdCard(merchant.getLegalPersonIdCard());
        dto.setContactEmail(merchant.getContactEmail());
        dto.setSettlementCycle(merchant.getSettlementCycle());
        dto.setAuditStatus(merchant.getAuditStatus());
        dto.setAuditRemark(merchant.getAuditRemark());
        dto.setStatus(merchant.getStatus());
        dto.setCreateTime(merchant.getCreateTime());
        dto.setUpdateTime(merchant.getUpdateTime());
        return dto;
    }
}
