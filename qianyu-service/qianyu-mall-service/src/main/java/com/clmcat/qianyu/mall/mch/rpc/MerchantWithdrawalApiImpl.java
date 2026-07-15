package com.clmcat.qianyu.mall.mch.rpc;

import com.clmcat.qianyu.mall.api.mch.MerchantAccountApi;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.MerchantWithdrawalApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageQueryDTO;
import com.clmcat.qianyu.mall.api.mch.model.dto.WithdrawalPageResultDto;
import com.clmcat.qianyu.mall.mch.mapper.FundOpLogMapper;
import com.clmcat.qianyu.mall.mch.mapper.MerchantWithdrawalMapper;
import com.clmcat.qianyu.mall.mch.model.entity.FundOpLog;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantWithdrawal;
import com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus;
import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // ==================== BG-02：资金类 op_log（同 @Transactional 强一致） ====================

    /** 资金 op_log 雪花 ID（workerId=53，与登录服务 42 等错开）。 */
    private static final CustomSnowflake FUND_OPLOG_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    private static final ObjectMapper OPLOG_MAPPER = new ObjectMapper();

    /** 反向读 admin 上下文（与 BackstageLoginVerifyFunction.ATTR_* 同值；mall-service 引不到 backstage 模块）。 */
    private static final String ATTR_ADMIN_ID = "admin:id";
    private static final String ATTR_USERNAME = "admin:username";

    /** 资金 op_log Mapper（映射 t_admin_op_log，同 @Transactional 写入，审计失败回滚资金）。 */
    @Resource
    private FundOpLogMapper fundOpLogMapper;

    @Override
    public com.clmcat.qianyu.mall.api.model.dto.PageResultDTO<WithdrawalPageResultDto> pageByPlatform(WithdrawalPageQueryDTO query) {
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
            return com.clmcat.qianyu.mall.api.model.dto.PageResultDTO.<WithdrawalPageResultDto>builder()
                    .records(Collections.emptyList()).total(page.getTotalRow())
                    .pageNum(page.getPageNumber()).pageSize(page.getPageSize()).build();
        }

        // 富化 merchantName / accountBalance
        List<WithdrawalPageResultDto> records = page.getRecords().stream()
                .map(w -> toResultDto(w, true)).collect(java.util.stream.Collectors.toList());
        return com.clmcat.qianyu.mall.api.model.dto.PageResultDTO.<WithdrawalPageResultDto>builder()
                .records(records).total(page.getTotalRow())
                .pageNum(page.getPageNumber()).pageSize(page.getPageSize()).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long withdrawalId) {
        long start = System.currentTimeMillis();
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
        BigDecimal amt = withdrawal.getAmount();
        Map<String, Object> before = fundBefore(withdrawal, account); // BG-02：落账前快照
        boolean ok = accountApi.settleForApprove(withdrawal.getMerchantId(), amt, account.getVersion());
        if (!ok) {
            log.warn("approve 账户 settleForApprove CAS 失败 withdrawalId={} merchantId={}（frozen/version 冲突）",
                    withdrawalId, withdrawal.getMerchantId());
            MchStatus.MCH_WITHDRAWAL_CAS_FAIL.assertThrowResEx(true);
        }
        // BG-02：after 快照（status→1；frozen−amt / totalWithdraw+amt / version+1）+ 同事务 op_log
        Map<String, Object> afterW = withdrawalView(withdrawal); afterW.put("status", 1);
        Map<String, Object> afterA = accountView(account);
        afterA.put("frozenAmount", nullSafe(account.getFrozenAmount()).subtract(amt));
        afterA.put("totalWithdraw", nullSafe(account.getTotalWithdraw()).add(amt));
        afterA.put("version", account.getVersion() == null ? null : account.getVersion() + 1);
        writeFundOpLog("mch:withdrawal:approve", withdrawalId,
                jsonOf(before), jsonOf(fundSnap(afterW, afterA)), start);
        log.info("approve 提现单审批通过 withdrawalId={} merchantId={} amount={}",
                withdrawalId, withdrawal.getMerchantId(), withdrawal.getAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long withdrawalId, String rejectReason) {
        long start = System.currentTimeMillis();
        MchStatus.MCH_WITHDRAWAL_NOT_FOUND.assertThrowResEx(withdrawalId == null || withdrawalId <= 0);
        MerchantWithdrawal withdrawal = withdrawalMapper.selectOneById(withdrawalId);
        MchStatus.MCH_WITHDRAWAL_NOT_FOUND.assertThrowResEx(withdrawal == null);
        Integer fromStatus = withdrawal.getStatus();
        // reject 允许源：0/1/2 → 4
        boolean fromAllowed = fromStatus != null && (fromStatus == 0 || fromStatus == 1 || fromStatus == 2);
        MchStatus.MCH_WITHDRAWAL_STATUS_INVALID.assertThrowResEx(!fromAllowed);

        BigDecimal amt = withdrawal.getAmount();
        MerchantAccount account = null;        // 仅 1/2 需退款，0→4 不动账户
        Map<String, Object> before;            // BG-02 落账前快照
        // 资金回退：1→4 / 2→4 需 refundForReject；0→4 仅状态变更（0 时资金已在 apply 阶段冻结，
        // 按状态机契约 0→4 不动账户；如运营发现 0 单有问题应直接拒绝且不再流转资金）
        if (fromStatus == 1 || fromStatus == 2) {
            account = accountApi.selectAccountByMerchantId(withdrawal.getMerchantId());
            MchStatus.MCH_WITHDRAWAL_CAS_FAIL.assertThrowResEx(account == null);
            before = fundBefore(withdrawal, account);
            boolean ok = accountApi.refundForReject(withdrawal.getMerchantId(), amt, account.getVersion());
            if (!ok) {
                log.warn("reject 账户 refundForReject CAS 失败 withdrawalId={} merchantId={}（frozen/version 冲突）",
                        withdrawalId, withdrawal.getMerchantId());
                MchStatus.MCH_WITHDRAWAL_CAS_FAIL.assertThrowResEx(true);
            }
        } else {
            before = fundBefore(withdrawal, null); // 0→4 无账户变动
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
        // BG-02：after 快照（status→4 + rejectReason；1/2 退款 balance+frozen）+ 同事务 op_log
        Map<String, Object> afterW = withdrawalView(withdrawal);
        afterW.put("status", 4);
        afterW.put("rejectReason", rejectReason);
        Map<String, Object> afterA = null;
        if (account != null) {
            afterA = accountView(account);
            afterA.put("balance", nullSafe(account.getBalance()).add(amt));
            afterA.put("frozenAmount", nullSafe(account.getFrozenAmount()).subtract(amt));
            afterA.put("version", account.getVersion() == null ? null : account.getVersion() + 1);
        }
        writeFundOpLog("mch:withdrawal:reject", withdrawalId, jsonOf(before), jsonOf(fundSnap(afterW, afterA)), start);
        log.info("reject 提现单拒绝 withdrawalId={} merchantId={} fromStatus={} reason={}",
                withdrawalId, withdrawal.getMerchantId(), fromStatus, rejectReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markTransferred(Long withdrawalId, String transferNo, Boolean success) {
        long start = System.currentTimeMillis();
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
        // BG-02：before/after 快照（仅 withdrawal，不动账户）+ 同事务 op_log
        Map<String, Object> before = fundSnap(withdrawalView(withdrawal), null);
        Map<String, Object> afterW = withdrawalView(withdrawal);
        afterW.put("status", toStatus);
        afterW.put("transferNo", transferNo);
        if (Boolean.TRUE.equals(success)) {
            afterW.put("transferTime", now);
        }
        writeFundOpLog("mch:withdrawal:transfer", withdrawalId,
                jsonOf(before), jsonOf(fundSnap(afterW, null)), start);
        log.info("markTransferred 打款标记 withdrawalId={} transferNo={} success={}",
                withdrawalId, transferNo, success);
    }

    // ==================== BG-02：资金类 op_log（同 @Transactional 强一致） ====================

    /**
     * 资金 op_log 写入（两段 CAS 均成功后调用）。
     * <p>与主流程同 {@code @Transactional}：{@code fundOpLogMapper.insert} 抛错会随主事务回滚已落账的资金 CAS，
     * 满足合并门禁「审计失败→资金回滚」（决策 2：A 强一致）。
     */
    private void writeFundOpLog(String permCode, Long withdrawalId, String beforeJson, String afterJson, long startMs) {
        AdminContext ctx = currentAdminContext();
        long now = System.currentTimeMillis();
        FundOpLog opLog = new FundOpLog();
        opLog.setId(FUND_OPLOG_ID_SNOWFLAKE.nextId());
        opLog.setAccountId(ctx.adminId() == null ? 0L : ctx.adminId());
        opLog.setUsername(ctx.username() == null ? "" : ctx.username());
        opLog.setPermCode(permCode);
        opLog.setTargetEntity("MerchantWithdrawal");
        opLog.setTargetId(withdrawalId == null ? null : String.valueOf(withdrawalId));
        opLog.setBeforeJson(beforeJson);
        opLog.setAfterJson(afterJson);
        opLog.setIp(ctx.ip());
        opLog.setUserAgent(ctx.userAgent());
        opLog.setTs(now);
        opLog.setResult(1); // 到达此处即两段 CAS 均成功
        opLog.setCostMs((int) (now - startMs));
        opLog.setCreateTime(now);
        fundOpLogMapper.insert(opLog);
    }

    /**
     * 反向读运营上下文（adminId/username/ip/userAgent）。
     * <p>单体 in-JVM Dubbo 跑在 HTTP 请求线程，{@link RequestContextHolder} 可读到 backstage
     * {@code BackstageLoginVerifyFunction} 注入的 {@code admin:id}/{@code admin:username}；
     * 无 web 上下文（如真实网络 Dubbo）则降级为占位值（adminId=0/username=空），审计仍可落库。
     */
    private AdminContext currentAdminContext() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return new AdminContext(0L, "", null, null);
            HttpServletRequest req = attrs.getRequest();
            Object id = req.getAttribute(ATTR_ADMIN_ID);
            Long adminId = id instanceof Number ? ((Number) id).longValue() : 0L;
            Object name = req.getAttribute(ATTR_USERNAME);
            return new AdminContext(adminId, name instanceof String ? (String) name : "",
                    req.getRemoteAddr(), req.getHeader("User-Agent"));
        } catch (Exception e) {
            return new AdminContext(0L, "", null, null);
        }
    }

    private String jsonOf(Object o) {
        try {
            return OPLOG_MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, Object> withdrawalView(MerchantWithdrawal w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", w.getId());
        m.put("withdrawalNo", w.getWithdrawalNo());
        m.put("merchantId", w.getMerchantId());
        m.put("amount", w.getAmount());
        m.put("status", w.getStatus());
        return m;
    }

    private static Map<String, Object> accountView(MerchantAccount a) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (a == null) return m;
        m.put("balance", a.getBalance());
        m.put("frozenAmount", a.getFrozenAmount());
        m.put("totalWithdraw", a.getTotalWithdraw());
        m.put("version", a.getVersion());
        return m;
    }

    /** before 快照：withdrawal + account（account 为 null 时置 null，表示该路径不动账户）。 */
    private static Map<String, Object> fundBefore(MerchantWithdrawal w, MerchantAccount a) {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("withdrawal", withdrawalView(w));
        snap.put("account", a == null ? null : accountView(a));
        return snap;
    }

    /** 组装快照外层 {withdrawal, account}。 */
    private static Map<String, Object> fundSnap(Map<String, Object> wv, Map<String, Object> av) {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("withdrawal", wv);
        snap.put("account", av);
        return snap;
    }

    private static BigDecimal nullSafe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private record AdminContext(Long adminId, String username, String ip, String userAgent) {}

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
