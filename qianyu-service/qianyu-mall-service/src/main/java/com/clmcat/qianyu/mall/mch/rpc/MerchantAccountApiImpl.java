package com.clmcat.qianyu.mall.mch.rpc;

import com.clmcat.qianyu.mall.api.mch.MerchantAccountApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantAccountDto;
import com.clmcat.qianyu.mall.mch.mapper.MerchantAccountMapper;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@DubboService
@Service
public class MerchantAccountApiImpl implements MerchantAccountApi {

    @Resource
    private MerchantAccountMapper accountMapper;

    @Override
    public MerchantAccountDto getByMerchantId(Long merchantId) {
        MerchantAccount account = accountMapper.selectByMerchantId(merchantId);
        return toDto(account);
    }

    @Override
    public MerchantAccountDto getById(Long accountId) {
        MerchantAccount account = accountMapper.selectOneById(accountId);
        return toDto(account);
    }

    private MerchantAccountDto toDto(MerchantAccount account) {
        if (account == null) return null;
        MerchantAccountDto dto = new MerchantAccountDto();
        dto.setId(account.getId());
        dto.setMerchantId(account.getMerchantId());
        dto.setBalance(account.getBalance());
        dto.setFrozenAmount(account.getFrozenAmount());
        dto.setTotalIncome(account.getTotalIncome());
        dto.setTotalWithdraw(account.getTotalWithdraw());
        dto.setTotalRefund(account.getTotalRefund());
        dto.setTotalCommission(account.getTotalCommission());
        dto.setVersion(account.getVersion());
        dto.setCreateTime(account.getCreateTime());
        dto.setUpdateTime(account.getUpdateTime());
        return dto;
    }

    // ==================== Internal methods for ViewBiz ====================

    public com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount selectAccountByMerchantId(Long merchantId) {
        return accountMapper.selectByMerchantId(merchantId);
    }

    public void updateAccount(com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount account) {
        accountMapper.update(account);
    }
}
