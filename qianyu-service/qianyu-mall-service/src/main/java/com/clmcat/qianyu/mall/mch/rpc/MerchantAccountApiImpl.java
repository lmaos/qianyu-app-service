package com.clmcat.qianyu.mall.mch.rpc;

import com.clmcat.qianyu.mall.api.mch.MerchantAccountApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantAccountDto;
import com.clmcat.qianyu.mall.mch.mapper.MerchantAccountMapper;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

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

    /**
     * P0-5: 账户余额 CAS 扣减（防并发超额提现）
     * WHERE merchant_id + balance + version，affected=0 说明并发冲突或余额不足
     * @return true 如果 CAS 成功
     */
    public boolean deductForWithdraw(Long merchantId, BigDecimal amount, BigDecimal currentBalance, BigDecimal currentFrozen, Long currentVersion) {
        com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount update = new com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount();
        update.setBalance(currentBalance.subtract(amount));
        update.setFrozenAmount(currentFrozen.add(amount));
        update.setVersion(currentVersion + 1);
        update.setUpdateTime(System.currentTimeMillis());
        int affected = accountMapper.updateByQuery(update,
                QueryWrapper.create().where("merchant_id = " + merchantId)
                        .and("balance = " + currentBalance.toPlainString())
                        .and("version = " + currentVersion));
        return affected > 0;
    }
}
