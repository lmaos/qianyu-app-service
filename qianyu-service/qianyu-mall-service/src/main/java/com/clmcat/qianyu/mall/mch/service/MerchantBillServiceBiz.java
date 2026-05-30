package com.clmcat.qianyu.mall.mch.service;

import com.clmcat.qianyu.mall.api.mch.MerchantBillApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantAccountDto;
import com.clmcat.qianyu.mall.mch.mapper.MerchantAccountMapper;
import com.clmcat.qianyu.mall.mch.mapper.MerchantBillMapper;
import com.clmcat.qianyu.mall.mch.mapper.MerchantWithdrawalMapper;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantBill;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantWithdrawal;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@DubboService
@Service
public class MerchantBillServiceBiz implements MerchantBillApi {

    @Resource
    private MerchantAccountMapper accountMapper;

    @Resource
    private MerchantBillMapper billMapper;

    @Resource
    private MerchantWithdrawalMapper withdrawalMapper;

    @Override
    public MerchantAccountDto getByMerchantId(Long merchantId) {
        MerchantAccount account = accountMapper.selectByMerchantId(merchantId);
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

    public Page<MerchantBill> paginateBills(Page<MerchantBill> page, QueryWrapper qw) {
        return billMapper.paginate(page, qw);
    }

    public Page<Map<String, Object>> selectSettlementPage(Page<Map<String, Object>> page, Long merchantId, Integer status) {
        return billMapper.selectSettlementPage(page, merchantId, status);
    }

    public Page<MerchantWithdrawal> paginateWithdrawals(Page<MerchantWithdrawal> page, QueryWrapper qw) {
        return withdrawalMapper.paginate(page, qw);
    }

    public void insertWithdrawal(MerchantWithdrawal withdrawal) {
        withdrawalMapper.insertSelective(withdrawal);
    }
}
