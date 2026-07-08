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

    // ==================== 资金三原语（双 CAS · account 侧） ====================

    @Override
    public boolean freezeForApply(Long merchantId, BigDecimal amount, Long version) {
        if (merchantId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || version == null) {
            return false;
        }
        // 读出当前 balance/frozen 用于双 CAS WHERE（version 单独的 CAS 不足以判定余额/frozen 真值）
        MerchantAccount account = accountMapper.selectByMerchantId(merchantId);
        if (account == null || account.getVersion() == null || !account.getVersion().equals(version)) {
            return false;
        }
        BigDecimal currentBalance = account.getBalance();
        BigDecimal currentFrozen = account.getFrozenAmount();
        if (currentBalance == null || currentBalance.compareTo(amount) < 0) {
            return false; // 余额不足
        }
        MerchantAccount update = new MerchantAccount();
        update.setBalance(currentBalance.subtract(amount));
        update.setFrozenAmount(currentFrozen.add(amount));
        update.setVersion(version + 1);
        update.setUpdateTime(System.currentTimeMillis());
        int affected = accountMapper.updateByQuery(update,
                QueryWrapper.create().where("merchant_id = ?", merchantId)
                        .and("balance = ?", currentBalance)
                        .and("frozen_amount = ?", currentFrozen)
                        .and("version = ?", version));
        return affected > 0;
    }

    @Override
    public boolean settleForApprove(Long merchantId, BigDecimal amount, Long version) {
        if (merchantId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || version == null) {
            return false;
        }
        MerchantAccount account = accountMapper.selectByMerchantId(merchantId);
        if (account == null || account.getVersion() == null || !account.getVersion().equals(version)) {
            return false;
        }
        BigDecimal currentFrozen = account.getFrozenAmount();
        BigDecimal currentTotalWithdraw = account.getTotalWithdraw();
        if (currentFrozen == null || currentFrozen.compareTo(amount) < 0) {
            return false; // frozen 不足
        }
        MerchantAccount update = new MerchantAccount();
        update.setFrozenAmount(currentFrozen.subtract(amount));
        update.setTotalWithdraw((currentTotalWithdraw == null ? BigDecimal.ZERO : currentTotalWithdraw).add(amount));
        update.setVersion(version + 1);
        update.setUpdateTime(System.currentTimeMillis());
        int affected = accountMapper.updateByQuery(update,
                QueryWrapper.create().where("merchant_id = ?", merchantId)
                        .and("frozen_amount = ?", currentFrozen)
                        .and("version = ?", version));
        return affected > 0;
    }

    @Override
    public boolean refundForReject(Long merchantId, BigDecimal amount, Long version) {
        if (merchantId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || version == null) {
            return false;
        }
        MerchantAccount account = accountMapper.selectByMerchantId(merchantId);
        if (account == null || account.getVersion() == null || !account.getVersion().equals(version)) {
            return false;
        }
        BigDecimal currentBalance = account.getBalance();
        BigDecimal currentFrozen = account.getFrozenAmount();
        if (currentFrozen == null || currentFrozen.compareTo(amount) < 0) {
            return false; // frozen 不足
        }
        MerchantAccount update = new MerchantAccount();
        update.setBalance((currentBalance == null ? BigDecimal.ZERO : currentBalance).add(amount));
        update.setFrozenAmount(currentFrozen.subtract(amount));
        update.setVersion(version + 1);
        update.setUpdateTime(System.currentTimeMillis());
        int affected = accountMapper.updateByQuery(update,
                QueryWrapper.create().where("merchant_id = ?", merchantId)
                        .and("frozen_amount = ?", currentFrozen)
                        .and("version = ?", version));
        return affected > 0;
    }

    /**
     * P0-5: 账户余额 CAS 扣减（防并发超额提现）。
     * <p>历史 impl 内方法，现委派 {@link #freezeForApply}（资金三原语契约化后 deductForWithdraw 仅保留过渡）。
     * <p>WHERE merchant_id + balance + version，affected=0 说明并发冲突或余额不足。
     * @return true 如果 CAS 成功
     */
    public boolean deductForWithdraw(Long merchantId, BigDecimal amount, BigDecimal currentBalance, BigDecimal currentFrozen, Long currentVersion) {
        return freezeForApply(merchantId, amount, currentVersion);
    }
}
