package com.clmcat.qianyu.mall.mch.rpc;

import com.clmcat.qianyu.mall.api.mch.MerchantAccountApi;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.MerchantWithdrawalApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageQueryDTO;
import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageResultDto;
import com.clmcat.qianyu.mall.mch.mapper.MerchantWithdrawalMapper;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantWithdrawal;
import com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 提现审批闭环 RPC 实现（运营端）。
 * <p>编排账户三原语（{@link MerchantAccountApi#settleForApprove}/{@link MerchantAccountApi#refundForReject}）
 * + 提现单双 CAS（id + fromStatus），approve/reject 在同一 {@code @Transactional} 内任一 affected=0 回滚。
 * <p>状态机：0待审/1审核通过/2打款中/3打款成功/4审核拒绝/5打款失败。
 * <p>同模块进程内直调 {@code MerchantAccountApiImpl}（{@link MerchantAccountApi} 同 bean，{@code @Resource}），
 * 查 merchantName 走 {@code @DubboReference MerchantApi}（跨子域）。
 */
@Slf4j
@DubboService
@Service
public class MerchantWithdrawalApiImpl implements MerchantWithdrawalApi {

    @Resource
    private MerchantWithdrawalMapper withdrawalMapper;

    /** 同模块进程内直调，不走 Dubbo（资金三原语双 CAS 实现）。 */
    @Resource
    private MerchantAccountApiImpl accountApi;

    /** 跨子域（merchant 域）查 merchantName，走 Dubbo。 */
    @DubboReference
    private MerchantApi merchantApi;

    @Override
    public List<WithdrawalPageResultDto> pageByPlatform(WithdrawalPageQueryDTO query) {
        QueryWrapper qw = QueryWrapper.create();
        if (query.getMerchantId() != null) {
            qw.and("merchant_id = ?", query.getMerchantId());
        }
        if (query.getStatus() != null) {
            qw.and("status = ?", query.getStatus());
        }
        if (query.getWithdrawalNo() != null && !query.getWithdrawalNo().isEmpty()) {
            qw.and("withdrawal_no like ?", "%" + query.getWithdrawalNo() + "%");
        }
        if (query.getCreateTimeStart() != null) {
            qw.and("create_time >= ?", query.getCreateTimeStart());
        }
        if (query.getCreateTimeEnd() != null) {
            qw.and("create_time <= ?", query.getCreateTimeEnd());
        }
        qw.orderBy("create_time DESC");

        int pageNum = query.getPageNum() != null && query.getPageNum() > 0 ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null && query.getPageSize() > 0 ? query.getPageSize() : 10;

        Page<MerchantWithdrawal> page = withdrawalMapper.paginate(Page.of(pageNum, pageSize), qw);
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return Collections.emptyList();
        }

        // 富化 merchantName / accountBalance
        return page.getRecords().stream().map(w -> toResultDto(w, true)).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long withdrawalId) {
        MchStatus.MCH_WITHDRAWAL_NOT_FOUND.assertThrowResEx(withdrawalId == null || withdrawalId <= 0);
        MerchantWithdrawal withdrawal = withdrawalMapper.selectOneById(withdrawalId);
        MchStatus.MCH_WITHDRAWAL_NOT_FOUND.assertThrowResEx(withdrawal == null);
        // 状态校验：仅 0 待审 可审批通过
        MchStatus.MCH_WITHDRAWAL_STATUS_INVALID.assertThrowResEx(
                withdrawal.getStatus() == null || withdrawal.getStatus() != 0);

        // 提现单 CAS：0 → 1（WHERE id + status=0）
        MerchantWithdrawal update = new MerchantWithdrawal();
        update.setStatus(1);
        update.setUpdateTime(System.currentTimeMillis());
        int wAffected = withdrawalMapper.updateByQuery(update,
                QueryWrapper.create().where("id = ?", withdrawalId).and("status = ?", 0));
        if (wAffected <= 0) {
            log.warn("approve 提现单 CAS 失败 withdrawalId={}（已被并发改动）", withdrawalId);
            MchStatus.MCH_WITHDRAWAL_CAS_FAIL.assertThrowResEx(true);
        }

        // 账户 CAS：settleForApprove（frozen→totalWithdraw + version+1）
        MerchantAccount account = accountApi.selectAccountByMerchantId(withdrawal.getMerchantId());
        MchStatus.MCH_WITHDRAWAL_CAS_FAIL.assertThrowResEx(account == null);
        boolean ok = accountApi.settleForApprove(
                withdrawal.getMerchantId(), withdrawal.getAmount(), account.getVersion());
        if (!ok) {
            log.warn("approve 账户 settleForApprove CAS 失败 withdrawalId={} merchantId={}（frozen/version 冲突）",
                    withdrawalId, withdrawal.getMerchantId());
            MchStatus.MCH_WITHDRAWAL_CAS_FAIL.assertThrowResEx(true);
        }
        log.info("approve 提现单审批通过 withdrawalId={} merchantId={} amount={}",
                withdrawalId, withdrawal.getMerchantId(), withdrawal.getAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long withdrawalId, String rejectReason) {
        MchStatus.MCH_WITHDRAWAL_NOT_FOUND.assertThrowResEx(withdrawalId == null || withdrawalId <= 0);
        MerchantWithdrawal withdrawal = withdrawalMapper.selectOneById(withdrawalId);
        MchStatus.MCH_WITHDRAWAL_NOT_FOUND.assertThrowResEx(withdrawal == null);
        Integer fromStatus = withdrawal.getStatus();
        // reject 允许源：0/1/2 → 4
        boolean fromAllowed = fromStatus != null && (fromStatus == 0 || fromStatus == 1 || fromStatus == 2);
        MchStatus.MCH_WITHDRAWAL_STATUS_INVALID.assertThrowResEx(!fromAllowed);

        // 资金回退：1→4 / 2→4 需 refundForReject；0→4 仅状态变更（尚未冻结资金——0 时资金已在 apply 阶段冻结，
        // 按状态机契约 0→4 不动账户；如运营发现 0 单有问题应直接拒绝且不再流转资金）
        if (fromStatus == 1 || fromStatus == 2) {
            MerchantAccount account = accountApi.selectAccountByMerchantId(withdrawal.getMerchantId());
            MchStatus.MCH_WITHDRAWAL_CAS_FAIL.assertThrowResEx(account == null);
            boolean ok = accountApi.refundForReject(
                    withdrawal.getMerchantId(), withdrawal.getAmount(), account.getVersion());
            if (!ok) {
                log.warn("reject 账户 refundForReject CAS 失败 withdrawalId={} merchantId={}（frozen/version 冲突）",
                        withdrawalId, withdrawal.getMerchantId());
                MchStatus.MCH_WITHDRAWAL_CAS_FAIL.assertThrowResEx(true);
            }
        }

        // 提现单 CAS：fromStatus → 4
        MerchantWithdrawal update = new MerchantWithdrawal();
        update.setStatus(4);
        update.setRejectReason(rejectReason);
        update.setUpdateTime(System.currentTimeMillis());
        int wAffected = withdrawalMapper.updateByQuery(update,
                QueryWrapper.create().where("id = ?", withdrawalId).and("status = ?", fromStatus));
        if (wAffected <= 0) {
            log.warn("reject 提现单 CAS 失败 withdrawalId={} fromStatus={}（已被并发改动）",
                    withdrawalId, fromStatus);
            MchStatus.MCH_WITHDRAWAL_CAS_FAIL.assertThrowResEx(true);
        }
        log.info("reject 提现单拒绝 withdrawalId={} merchantId={} fromStatus={} reason={}",
                withdrawalId, withdrawal.getMerchantId(), fromStatus, rejectReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markTransferred(Long withdrawalId, String transferNo, Boolean success) {
        MchStatus.MCH_WITHDRAWAL_NOT_FOUND.assertThrowResEx(withdrawalId == null || withdrawalId <= 0);
        MchStatus.MCH_WITHDRAWAL_STATUS_INVALID.assertThrowResEx(
                transferNo == null || transferNo.isEmpty() || success == null);
        // transferNo 全局唯一预查（DB UNIQUE 索引兜底，见 02-tech-design.md L374）
        long dupCount = withdrawalMapper.selectCountByQuery(
                QueryWrapper.create().where("transfer_no = ?", transferNo)
                        .and("id <> ?", withdrawalId));
        MchStatus.MCH_WITHDRAWAL_TRANSFER_NO_DUPLICATE.assertThrowResEx(dupCount > 0);

        MerchantWithdrawal withdrawal = withdrawalMapper.selectOneById(withdrawalId);
        MchStatus.MCH_WITHDRAWAL_NOT_FOUND.assertThrowResEx(withdrawal == null);
        // 仅 2 打款中 可标记结果
        MchStatus.MCH_WITHDRAWAL_STATUS_INVALID.assertThrowResEx(
                withdrawal.getStatus() == null || withdrawal.getStatus() != 2);

        int toStatus = Boolean.TRUE.equals(success) ? 3 : 5;
        long now = System.currentTimeMillis();
        MerchantWithdrawal update = new MerchantWithdrawal();
        update.setStatus(toStatus);
        update.setTransferNo(transferNo);
        if (Boolean.TRUE.equals(success)) {
            update.setTransferTime(now);
        }
        update.setUpdateTime(now);
        int wAffected = withdrawalMapper.updateByQuery(update,
                QueryWrapper.create().where("id = ?", withdrawalId).and("status = ?", 2));
        if (wAffected <= 0) {
            log.warn("markTransferred 提现单 CAS 失败 withdrawalId={}（已被并发改动）", withdrawalId);
            MchStatus.MCH_WITHDRAWAL_CAS_FAIL.assertThrowResEx(true);
        }
        log.info("markTransferred 打款标记 withdrawalId={} transferNo={} success={}",
                withdrawalId, transferNo, success);
    }

    // ==================== private ====================

    private WithdrawalPageResultDto toResultDto(MerchantWithdrawal w, boolean enrich) {
        WithdrawalPageResultDto dto = new WithdrawalPageResultDto();
        dto.setId(w.getId());
        dto.setWithdrawalNo(w.getWithdrawalNo());
        dto.setMerchantId(w.getMerchantId());
        dto.setAmount(w.getAmount());
        dto.setBankName(w.getBankName());
        dto.setBankAccount(w.getBankAccount());
        dto.setAccountName(w.getAccountName());
        dto.setStatus(w.getStatus());
        dto.setRejectReason(w.getRejectReason());
        dto.setTransferNo(w.getTransferNo());
        dto.setTransferTime(w.getTransferTime());
        dto.setCreateTime(w.getCreateTime());
        dto.setAllowedActions(computeAllowedActions(w.getStatus()));

        if (enrich) {
            // merchantName 富化（跨子域 Dubbo，失败降级为 null 不阻断列表）
            try {
                MerchantDto m = merchantApi.getById(w.getMerchantId());
                if (m != null) {
                    dto.setMerchantName(m.getName());
                }
            } catch (Exception e) {
                log.warn("pageByPlatform 富化 merchantName 失败 merchantId={}: {}", w.getMerchantId(), e.getMessage());
            }
            // 审批时余额快照（同模块直调，失败降级 null）
            try {
                MerchantAccount account = accountApi.selectAccountByMerchantId(w.getMerchantId());
                if (account != null) {
                    dto.setAccountBalance(account.getBalance());
                }
            } catch (Exception e) {
                log.warn("pageByPlatform 读取账户余额失败 merchantId={}: {}", w.getMerchantId(), e.getMessage());
            }
        }
        return dto;
    }

    /** 按 status 计算可执行动作：0→[approve,reject]；1/2→[markTransferred,reject]；其余→[]。 */
    private List<String> computeAllowedActions(Integer status) {
        List<String> actions = new ArrayList<>();
        if (status == null) return actions;
        switch (status) {
            case 0:
                actions.add("approve");
                actions.add("reject");
                break;
            case 1:
            case 2:
                actions.add("markTransferred");
                actions.add("reject");
                break;
            default:
                break; // 3/4/5 终态无可执行动作
        }
        return actions;
    }
}
