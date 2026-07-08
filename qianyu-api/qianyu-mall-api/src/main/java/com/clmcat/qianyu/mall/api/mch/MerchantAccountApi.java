package com.clmcat.qianyu.mall.api.mch;

import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantAccountDto;

import java.math.BigDecimal;

public interface MerchantAccountApi {

    MerchantAccountDto getByMerchantId(Long merchantId);

    MerchantAccountDto getById(Long accountId);

    /**
     * 冻结提现金额（提现申请阶段·C 端）：balance -= amount, frozen += amount, version + 1。
     * <p>双 CAS 模型之一（账户侧）：WHERE merchant_id = ? AND version = ?，affected = 0 表示并发冲突或余额不足。
     * <p>调用方（如 withdrawApply）需自行保证传入 amount > 0 且 ≤ 当前 balance，本原语仅做 CAS 落账。
     * @param merchantId 商家 ID
     * @param amount     提现金额（元，>0）
     * @param version    当前账户版本号（查询时取出）
     * @return true 表示 CAS 成功；false 表示并发冲突或余额不足，调用方应回滚
     */
    boolean freezeForApply(Long merchantId, BigDecimal amount, Long version);

    /**
     * 结算提现（审核通过 0→1）：frozen -= amount, totalWithdraw += amount, version + 1。
     * <p>双 CAS 模型之一（账户侧）：WHERE merchant_id = ? AND version = ?，affected = 0 表示并发冲突。
     * <p>必须与提现单 CAS（id + fromStatus=0）在同一 {@code @Transactional} 内执行，任一 affected=0 回滚。
     * @param merchantId 商家 ID
     * @param amount     提现金额（元，>0）
     * @param version    当前账户版本号（查询时取出）
     * @return true 表示 CAS 成功；false 表示并发冲突或 frozen 不足
     */
    boolean settleForApprove(Long merchantId, BigDecimal amount, Long version);

    /**
     * 退还冻结（审核拒绝 1→4 / 打款中止 2→4）：frozen -= amount, balance += amount, version + 1。
     * <p>双 CAS 模型之一（账户侧）：WHERE merchant_id = ? AND version = ?，affected = 0 表示并发冲突。
     * <p>必须与提现单 CAS（id + fromStatus）在同一 {@code @Transactional} 内执行，任一 affected=0 回滚。
     * @param merchantId 商家 ID
     * @param amount     提现金额（元，>0）
     * @param version    当前账户版本号（查询时取出）
     * @return true 表示 CAS 成功；false 表示并发冲突或 frozen 不足
     */
    boolean refundForReject(Long merchantId, BigDecimal amount, Long version);
}
