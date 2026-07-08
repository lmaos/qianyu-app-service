package com.clmcat.qianyu.mall.api.mch;

import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageQueryDTO;
import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageResultDto;

import java.util.List;

/**
 * 提现审批闭环 RPC 契约（运营端·mch 域）。
 * <p>实现：{@code MerchantWithdrawalApiImpl}（@DubboService），编排账户三原语（freezeForApply/settleForApprove/refundForReject）
 * + 提现单双 CAS + op_log 审计（资金类同步落库）。
 * <p>状态机：0待审/1审核通过/2打款中/3打款成功/4审核拒绝/5打款失败。
 * <ul>
 *   <li>{@link #approve} 0→1，调 settleForApprove（frozen→totalWithdraw + version+1 双 CAS）</li>
 *   <li>{@link #reject} 0/1/2→4；1→4 与 2→4 调 refundForReject（frozen→balance + version+1 双 CAS）；0→4 仅状态变更</li>
 *   <li>{@link #markTransferred} 2→3 成功 / 2→5 失败，手工回填 transferNo（全局唯一）</li>
 * </ul>
 */
public interface MerchantWithdrawalApi {

    /**
     * 平台视角提现审批分页（跨店），含四要素脱敏 + merchantName + accountBalance + allowedActions 富化。
     * @param query 分页筛选（merchantId/status/withdrawalNo/createTime 区间/pageNum/pageSize）
     * @return 提现单 DTO 列表（分页返回 List，total/页码信息由调用方据需二次封装）
     */
    List<WithdrawalPageResultDto> pageByPlatform(WithdrawalPageQueryDTO query);

    /**
     * 审核通过（0→1）。调 settleForApprove（frozen→totalWithdraw + version+1 双 CAS）。
     * <p>双 CAS 在同一 {@code @Transactional} 内：提现单 CAS（id + fromStatus=0）+ 账户 CAS（merchant_id + version），
     * 任一 affected=0 抛错回滚。
     * @param withdrawalId 提现单 ID
     */
    void approve(Long withdrawalId);

    /**
     * 审核拒绝（0/1/2→4）。
     * <p>0→4 仅状态变更；1→4 与 2→4 调 refundForReject（frozen→balance + version+1 双 CAS）。
     * 双 CAS 在同一 {@code @Transactional} 内，任一 affected=0 抛错回滚。
     * @param withdrawalId  提现单 ID
     * @param rejectReason  拒绝原因（运营填写）
     */
    void reject(Long withdrawalId, String rejectReason);

    /**
     * 标记打款结果（2→3 成功 / 2→5 失败），手工回填 transferNo。
     * <p>transferNo 全局唯一（业务层预查 + DB UNIQUE 索引兜底）。
     * @param withdrawalId 提现单 ID
     * @param transferNo   打款流水号（全局唯一）
     * @param success      true→status=3 打款成功并写 transferTime；false→status=5 打款失败
     */
    void markTransferred(Long withdrawalId, String transferNo, Boolean success);
}
