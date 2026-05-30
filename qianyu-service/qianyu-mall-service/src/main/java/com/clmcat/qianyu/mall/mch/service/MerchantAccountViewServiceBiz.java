package com.clmcat.qianyu.mall.mch.service;

import com.clmcat.qianyu.mall.mch.model.dto.BillQueryDTO;
import com.clmcat.qianyu.mall.mch.model.dto.SettlementQueryDTO;
import com.clmcat.qianyu.mall.mch.model.dto.WithdrawApplyDTO;
import com.clmcat.qianyu.mall.mch.model.dto.WithdrawQueryDTO;
import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantBill;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantWithdrawal;
import com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus;
import com.clmcat.qianyu.mall.mch.model.vo.AccountInfoVO;
import com.clmcat.qianyu.mall.mch.model.vo.BillItemVO;
import com.clmcat.qianyu.mall.mch.model.vo.SettlementItemVO;
import com.clmcat.qianyu.mall.mch.model.vo.WithdrawItemVO;
import com.clmcat.qianyu.mall.mch.support.MerchantConvert;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MerchantAccountViewServiceBiz {

    @Resource
    private MerchantServiceBiz merchantServiceBiz;

    @Resource
    private MerchantAccountServiceBiz accountServiceBiz;

    @Resource
    private MerchantBillServiceBiz billServiceBiz;

    /**
     * 账户信息
     */
    public AccountInfoVO getAccountInfo(long userId) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        MerchantAccount account = accountServiceBiz.selectAccountByMerchantId(merchant.getId());
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(account == null);

        return MerchantConvert.toAccountInfoVO(merchant.getId(), account);
    }

    /**
     * 账单列表
     */
    public Page<BillItemVO> getBillList(long userId, BillQueryDTO dto) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        int pageNum = dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.and("merchant_id = " + merchant.getId());
        if (dto.getType() != null && dto.getType() > 0) {
            queryWrapper.and("type = " + dto.getType());
        }
        if (dto.getStatus() != null) {
            queryWrapper.and("status = " + dto.getStatus());
        }
        if (dto.getStartTime() != null && !dto.getStartTime().isEmpty()) {
            // TODO: 替换真实接口 - 时间范围过滤
        }
        if (dto.getEndTime() != null && !dto.getEndTime().isEmpty()) {
            // TODO: 替换真实接口 - 时间范围过滤
        }
        queryWrapper.orderBy("create_time DESC");

        Page<MerchantBill> page = Page.of(pageNum, pageSize);
        Page<MerchantBill> billPage = billServiceBiz.paginateBills(page, queryWrapper);

        List<BillItemVO> voList = new ArrayList<>();
        if (billPage.getRecords() != null) {
            for (MerchantBill bill : billPage.getRecords()) {
                BillItemVO vo = BillItemVO.builder()
                        .id(bill.getId())
                        .orderId(bill.getOrderId())
                        .orderNo(bill.getOrderNo())
                        .type(bill.getType())
                        .typeText(MerchantConvert.getBillTypeText(bill.getType()))
                        .orderAmount(MerchantConvert.decimalToStr(bill.getOrderAmount()))
                        .refundAmount(MerchantConvert.decimalToStr(bill.getRefundAmount()))
                        .platformFee(MerchantConvert.decimalToStr(bill.getPlatformFee()))
                        .platformRate(bill.getPlatformRate())
                        .anchorFee(MerchantConvert.decimalToStr(bill.getAnchorFee()))
                        .merchantIncome(MerchantConvert.decimalToStr(bill.getMerchantIncome()))
                        .status(bill.getStatus())
                        .createTime(MerchantConvert.formatTime(bill.getCreateTime()))
                        .build();
                voList.add(vo);
            }
        }

        Page<BillItemVO> result = new Page<>();
        result.setRecords(voList);
        result.setTotalRow(billPage.getTotalRow());
        result.setPageNumber(billPage.getPageNumber());
        result.setPageSize(billPage.getPageSize());
        return result;
    }

    /**
     * 结算单列表
     */
    public Page<SettlementItemVO> getSettlementList(long userId, SettlementQueryDTO dto) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        int pageNum = dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        Page<Map<String, Object>> page = Page.of(pageNum, pageSize);
        Page<Map<String, Object>> settlementPage = billServiceBiz.selectSettlementPage(page, merchant.getId(), dto.getStatus());

        if (settlementPage == null) {
            return new Page<>(pageNum, pageSize);
        }

        List<SettlementItemVO> voList = new ArrayList<>();
        if (settlementPage.getRecords() != null) {
            for (Map<String, Object> row : settlementPage.getRecords()) {
                SettlementItemVO vo = SettlementItemVO.builder()
                        .id(row.get("settlement_id") != null ? Long.valueOf(row.get("settlement_id").toString()) : null)
                        .settlementNo("STL" + row.get("settlement_id"))
                        .startTime(MerchantConvert.formatTime(row.get("start_time") != null ? Long.valueOf(row.get("start_time").toString()) : null))
                        .endTime(MerchantConvert.formatTime(row.get("end_time") != null ? Long.valueOf(row.get("end_time").toString()) : null))
                        .orderCount(row.get("order_count") != null ? Integer.valueOf(row.get("order_count").toString()) : 0)
                        .orderAmount(row.get("order_amount") != null ? row.get("order_amount").toString() : "0.00")
                        .refundCount(row.get("refund_count") != null ? Integer.valueOf(row.get("refund_count").toString()) : 0)
                        .refundAmount(row.get("refund_amount") != null ? row.get("refund_amount").toString() : "0.00")
                        .platformFee(row.get("platform_fee") != null ? row.get("platform_fee").toString() : "0.00")
                        .anchorFee(row.get("anchor_fee") != null ? row.get("anchor_fee").toString() : "0.00")
                        .settlementAmount(row.get("settlement_amount") != null ? row.get("settlement_amount").toString() : "0.00")
                        .status(dto.getStatus() != null ? dto.getStatus() : 0)
                        .settleTime(null)
                        .build();
                voList.add(vo);
            }
        }

        Page<SettlementItemVO> result = new Page<>();
        result.setRecords(voList);
        result.setTotalRow(settlementPage.getTotalRow());
        result.setPageNumber(settlementPage.getPageNumber());
        result.setPageSize(settlementPage.getPageSize());
        return result;
    }

    /**
     * 提现申请
     */
    public String withdrawApply(long userId, WithdrawApplyDTO dto) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        MerchantAccount account = accountServiceBiz.selectAccountByMerchantId(merchant.getId());
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(account == null);

        // 金额校验
        BigDecimal amount = new BigDecimal(dto.getAmount());
        MchStatus.MCH_WITHDRAW_AMOUNT_INVALID.assertThrowResEx(amount.compareTo(BigDecimal.ZERO) <= 0);
        MchStatus.MCH_INSUFFICIENT_BALANCE.assertThrowResEx(account.getBalance().compareTo(amount) < 0);

        long now = System.currentTimeMillis();

        // 冻结余额
        account.setBalance(account.getBalance().subtract(amount));
        account.setFrozenAmount(account.getFrozenAmount().add(amount));
        account.setUpdateTime(now);
        accountServiceBiz.updateAccount(account);

        // 生成提现单号
        String withdrawalNo = "WD" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date(now)) + String.format("%03d", (int) (merchant.getId() % 1000));

        // 插入提现记录
        MerchantWithdrawal withdrawal = new MerchantWithdrawal();
        withdrawal.setId(MerchantConvert.WITHDRAWAL_ID_SNOWFLAKE.nextId());
        withdrawal.setWithdrawalNo(withdrawalNo);
        withdrawal.setMerchantId(merchant.getId());
        withdrawal.setAmount(amount);
        withdrawal.setBankName(dto.getBankName());
        withdrawal.setBankAccount(dto.getBankAccount());
        withdrawal.setAccountName(dto.getAccountName());
        withdrawal.setStatus(0); // 待审核
        withdrawal.setCreateTime(now);
        withdrawal.setUpdateTime(now);
        billServiceBiz.insertWithdrawal(withdrawal);

        return withdrawalNo;
    }

    /**
     * 提现记录列表
     */
    public Page<WithdrawItemVO> getWithdrawList(long userId, WithdrawQueryDTO dto) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        int pageNum = dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.and("merchant_id = " + merchant.getId());
        if (dto.getStatus() != null) {
            queryWrapper.and("status = " + dto.getStatus());
        }
        queryWrapper.orderBy("create_time DESC");

        Page<MerchantWithdrawal> page = Page.of(pageNum, pageSize);
        Page<MerchantWithdrawal> withdrawPage = billServiceBiz.paginateWithdrawals(page, queryWrapper);

        List<WithdrawItemVO> voList = new ArrayList<>();
        if (withdrawPage.getRecords() != null) {
            for (MerchantWithdrawal w : withdrawPage.getRecords()) {
                voList.add(MerchantConvert.toWithdrawItemVO(w));
            }
        }

        Page<WithdrawItemVO> result = new Page<>();
        result.setRecords(voList);
        result.setTotalRow(withdrawPage.getTotalRow());
        result.setPageNumber(withdrawPage.getPageNumber());
        result.setPageSize(withdrawPage.getPageSize());
        return result;
    }
}
