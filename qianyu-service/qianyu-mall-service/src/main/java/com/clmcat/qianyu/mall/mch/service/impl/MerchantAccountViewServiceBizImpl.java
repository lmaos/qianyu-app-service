package com.clmcat.qianyu.mall.mch.service.impl;

import com.clmcat.qianyu.mall.mch.rpc.MerchantAccountApiImpl;
import com.clmcat.qianyu.mall.mch.rpc.MerchantBillApiImpl;
import com.clmcat.qianyu.mall.mch.rpc.MerchantApiImpl;
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
import com.clmcat.qianyu.mall.mch.service.MerchantAccountViewServiceBiz;

@Service
public class MerchantAccountViewServiceBizImpl implements MerchantAccountViewServiceBiz {

    @Resource
    private MerchantApiImpl merchantServiceBiz;

    @Resource
    private MerchantAccountApiImpl accountServiceBiz;

    @Resource
    private MerchantBillApiImpl billServiceBiz;

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
        queryWrapper.and("merchant_id = ?", merchant.getId());
        if (dto.getType() != null && dto.getType() > 0) {
            queryWrapper.and("type = ?", dto.getType());
        }
        if (dto.getStatus() != null) {
            queryWrapper.and("status = ?", dto.getStatus());
        }
        // 时间范围过滤：startTime/endTime 是 String，兼容「毫秒戳」与「yyyy-MM-dd[ HH:mm:ss]」两种格式
        // （MerchantBill.createTime 是 Long 毫秒戳；前端 merchant-finance.vue 暂未传，此处为未来 UI + e2e 预留）
        Long startMillis = parseTimeToMillis(dto.getStartTime());
        if (startMillis != null) {
            queryWrapper.and("create_time >= ?", startMillis);
        }
        Long endMillis = parseTimeToMillis(dto.getEndTime());
        if (endMillis != null) {
            queryWrapper.and("create_time <= ?", endMillis);
        }
        queryWrapper.orderBy("create_time DESC");

        Page<MerchantBill> page = Page.of(pageNum, pageSize);
        Page<MerchantBill> billPage = billServiceBiz.paginateBills(page, queryWrapper);
        if (billPage == null) {
            billPage = new Page<>();
        }

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
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
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

        // P0-5: CAS 扣减余额（防并发超额提现）
        boolean ok = accountServiceBiz.deductForWithdraw(merchant.getId(), amount, account.getBalance(), account.getFrozenAmount(), account.getVersion());
        MchStatus.MCH_INSUFFICIENT_BALANCE.assertThrowResEx(!ok);

        // P0-5: 用雪花 ID 生成提现单号（防同商家同秒同号）
        String withdrawalNo = "WD" + MerchantConvert.WITHDRAWAL_ID_SNOWFLAKE.nextId();

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
        queryWrapper.and("merchant_id = ?", merchant.getId());
        if (dto.getStatus() != null) {
            queryWrapper.and("status = ?", dto.getStatus());
        }
        queryWrapper.orderBy("create_time DESC");

        Page<MerchantWithdrawal> page = Page.of(pageNum, pageSize);
        Page<MerchantWithdrawal> withdrawPage = billServiceBiz.paginateWithdrawals(page, queryWrapper);
        if (withdrawPage == null) {
            withdrawPage = new Page<>();
        }

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

    /**
     * 把账单查询时间字符串解析为毫秒戳。兼容两种格式：
     * - 纯数字（毫秒戳，e2e/后端自测用）：直接 parseLong
     * - 日期时间串（"yyyy-MM-dd HH:mm:ss" 或 "yyyy-MM-dd"，未来 UI 用）：DateTimeFormatter 解析后转 millis
     * 解析失败返回 null（不过滤，与字段可选语义一致）。
     */
    private Long parseTimeToMillis(String time) {
        if (time == null || time.isEmpty()) return null;
        String t = time.trim();
        // 1. 纯数字 → 毫秒戳
        if (t.matches("\\d+")) {
            try {
                return Long.parseLong(t);
            } catch (NumberFormatException ignored) { }
        }
        // 2. 日期 / 日期时间串 → 转 millis
        try {
            if (t.length() <= 10) {
                java.time.LocalDate ld = java.time.LocalDate.parse(t, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(t, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return ldt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (java.time.format.DateTimeParseException ignored) { }
        return null;
    }
}
