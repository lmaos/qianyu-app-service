package com.clmcat.qianyu.payment.wallet.service;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.core.table.MonthTableRouter;
import com.clmcat.qianyu.payment.api.wallet.WalletApi;
import com.clmcat.qianyu.payment.api.wallet.model.dto.TransactionDto;
import com.clmcat.qianyu.payment.api.wallet.model.dto.TransactionListDto;
import com.clmcat.qianyu.payment.api.wallet.model.dto.WalletDto;
import com.clmcat.qianyu.payment.wallet.mapper.TransactionRecordMapper;
import com.clmcat.qianyu.payment.wallet.mapper.UserDailyStatsMapper;
import com.clmcat.qianyu.payment.wallet.mapper.UserWalletMapper;
import com.clmcat.qianyu.payment.wallet.model.entity.TransactionRecord;
import com.clmcat.qianyu.payment.wallet.model.entity.UserWallet;
import com.clmcat.qianyu.payment.wallet.support.WalletSupport;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 虚拟货币钱包核心业务服务。
 * <p>
 * 实现 {@link WalletApi}，通过 Dubbo RPC 暴露。
 * <p>
 * 并发设计：
 * <ul>
 *   <li><b>INSERT-first</b>：先插入流水抢占幂等键，再执行资金操作。
 *       如果 INSERT 失败（并发冲突），资金尚未变动，安全返回已有结果。</li>
 *   <li>使用原子 SQL（{@code SET balance = balance +/- ?}）操作余额，无 SELECT FOR UPDATE</li>
 *   <li>支出时 {@code WHERE balance >= amount} 在数据库引擎层面防止透支</li>
 *   <li>冻结时 {@code WHERE balance >= amount} 同样在 DB 层面防并发超卖</li>
 * </ul>
 * <p>
 * 容错设计：
 * <ul>
 *   <li>余额变更与流水记录在同一事务内，@Transactional 保证原子性</li>
 *   <li>钱包不存在时自动创建（首次操作时）</li>
 * </ul>
 *
 * @author ark-home
 * @date 2026-08-03
 */
@DubboService
@Service
public class WalletServiceBiz extends WalletSupport implements WalletApi {

    private static final CustomSnowflake TRANS_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    private static final int MAX_LIMIT = 50;

    /** 默认单日消费上限（最小单位），可通过 Nacos 覆盖 */
    private static final long DEFAULT_DAILY_LIMIT = 50_000_00L; // 50000.00 虚拟币

    private static final String TABLE_PREFIX = "transaction_record";

    @Resource
    private UserWalletMapper userWalletMapper;

    @Resource
    private TransactionRecordMapper transactionRecordMapper;

    @Resource
    private UserDailyStatsMapper userDailyStatsMapper;

    // ==================== 下单模式：冻结 / 确认冻结 / 解冻 ====================

    /**
     * 冻结余额（下单时调用）。
     * <p>
     * 原子操作：balance -= amount, frozen_balance += amount。
     * WHERE balance >= amount 保证并发安全。
     * <p>
     * 不创建交易流水（流水在 confirm 时创建）。
     *
     * @param userId 用户ID
     * @param amount 冻结金额
     * @throws ApiException 余额不足时抛出
     */
    @Transactional
    public void freeze(long userId, long amount) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(amount <= 0, "金额必须大于0");

        long now = System.currentTimeMillis();

        // 快速检查钱包状态（冻结则拒绝）
        UserWallet wallet = userWalletMapper.customSelectByUserId(userId);
        if (wallet != null && wallet.getStatus() != null && wallet.getStatus() != 1) {
            ResponseStatus.U_FREEZE.assertThrowResEx(true, "账户已被冻结，无法操作");
        }

        // 原子冻结：balance → frozen_balance
        int affected = userWalletMapper.customFreeze(userId, amount, now);
        if (affected == 0) {
            if (wallet == null || wallet.getBalance() < amount) {
                ResponseStatus.R_ACCOUNT_LESS_MONEY.assertThrowResEx(true);
            }
            ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(true);
        }
    }

    /**
     * 确认冻结（确认扣款时调用）。
     * <p>
     * frozen_balance -= amount, total_expense += amount。
     * 创建虚拟币支出流水（INSERT-first 幂等）。
     * <p>
     * balance_before = 当前 balance + amount（回推冻结前余额）
     * balance_after  = 当前 balance（冻结后余额，确认后不变）
     *
     * @param userId        用户ID
     * @param amount        确认金额
     * @param bizType       业务类型
     * @param bizId         业务单号
     * @param idempotentKey 幂等键
     * @return 支出流水
     */
    @Transactional
    public TransactionDto confirmFreeze(long userId, long amount, String bizType, String bizId, String idempotentKey) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(amount <= 0, "金额必须大于0");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(bizType == null || bizType.trim().isEmpty(), "业务类型不能为空");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(idempotentKey == null || idempotentKey.trim().isEmpty(), "幂等键不能为空");

        long now = System.currentTimeMillis();
        long id = TRANS_ID_SNOWFLAKE.nextId();
        String transNo = allocateTransNo(id);
        String key = idempotentKey.trim();
        String tableName = MonthTableRouter.tableName(TABLE_PREFIX, now);

        // ① INSERT 流水先占坑（INSERT-first 幂等）
        TransactionRecord placeholder = buildPlaceholder(id, transNo, userId, TransactionRecord.TYPE_EXPENSE,
                amount, bizType, bizId, key, now);
        try {
            transactionRecordMapper.customInsert(tableName, placeholder);
        } catch (DuplicateKeyException e) {
            TransactionRecord dup = transactionRecordMapper.customSelectByIdempotentKey(tableName, key);
            if (dup != null) {
                return toDto(dup);
            }
            throw e;
        }

        // ② 读当前钱包，记录 pre-freeze 余额（用于回填流水）
        UserWallet walletBefore = userWalletMapper.customSelectByUserId(userId);
        long balanceAfter = walletBefore.getBalance();
        long balanceBefore = balanceAfter + amount; // 回推冻结前余额

        // ③ 确认冻结：frozen -= amount, total_expense += amount
        int affected = userWalletMapper.customConfirmFreeze(userId, amount, now);
        if (affected == 0) {
            ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(true, "确认冻结失败（解冻余额不足或钱包异常）");
        }

        // ④ 反写流水余额
        transactionRecordMapper.customUpdateBalances(tableName, id, balanceBefore, balanceAfter);

        placeholder.setBalanceBefore(balanceBefore);
        placeholder.setBalanceAfter(balanceAfter);
        return toDto(placeholder);
    }

    /**
     * 解冻余额（取消订单时调用）。
     * <p>
     * frozen_balance -= amount, balance += amount。
     * 不创建交易流水（钱只是解冻退回，未发生真实交易）。
     *
     * @param userId 用户ID
     * @param amount 解冻金额
     */
    @Transactional
    public void unfreeze(long userId, long amount) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(amount <= 0, "金额必须大于0");

        long now = System.currentTimeMillis();

        int affected = userWalletMapper.customUnfreeze(userId, amount, now);
        if (affected == 0) {
            ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(true, "解冻失败（冻结余额不足或钱包异常）");
        }
    }

    // ==================== 基础收/支/退（保持兼容） ====================

    @Override
    @Transactional
    public TransactionDto credit(long userId, long amount, String bizType, String bizId, String idempotentKey) {
        // 参数校验
        validateParams(userId, amount, bizType, idempotentKey);

        long now = System.currentTimeMillis();
        long id = TRANS_ID_SNOWFLAKE.nextId();
        String transNo = allocateTransNo(id);
        String key = idempotentKey.trim();
        String tableName = MonthTableRouter.tableName(TABLE_PREFIX, now);

        // ① INSERT 流水先占坑（占位余额=0）。失败 = 并发冲突 → 返回已有结果，资金未动
        TransactionRecord placeholder = buildPlaceholder(id, transNo, userId, TransactionRecord.TYPE_INCOME,
                amount, bizType, bizId, key, now);
        try {
            transactionRecordMapper.customInsert(tableName, placeholder);
        } catch (DuplicateKeyException e) {
            TransactionRecord dup = transactionRecordMapper.customSelectByIdempotentKey(tableName, key);
            if (dup != null) {
                return toDto(dup);
            }
            throw e;
        }

        // ② 原子收入（流水已占坑，失败会回滚整个事务包括①）
        int affected = userWalletMapper.customCredit(userId, amount, now);
        if (affected == 0) {
            ensureWalletExists(userId, now);
            affected = userWalletMapper.customCredit(userId, amount, now);
            if (affected == 0) {
                ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(true);
            }
        }

        // ③ 读取实际余额 → 反写流水
        UserWallet wallet = userWalletMapper.customSelectByUserId(userId);
        long balanceAfter = wallet.getBalance();
        long balanceBefore = balanceAfter - amount;
        transactionRecordMapper.customUpdateBalances(tableName, id, balanceBefore, balanceAfter);

        placeholder.setBalanceBefore(balanceBefore);
        placeholder.setBalanceAfter(balanceAfter);
        return toDto(placeholder);
    }

    @Override
    @Transactional
    public TransactionDto deduct(long userId, long amount, String bizType, String bizId, String idempotentKey) {
        // 参数校验
        validateParams(userId, amount, bizType, idempotentKey);

        long now = System.currentTimeMillis();
        long id = TRANS_ID_SNOWFLAKE.nextId();
        String transNo = allocateTransNo(id);
        String key = idempotentKey.trim();
        String tableName = MonthTableRouter.tableName(TABLE_PREFIX, now);

        // ① INSERT 流水先占坑（占位余额=0）。失败 = 并发冲突 → 返回已有结果，资金未动
        TransactionRecord placeholder = buildPlaceholder(id, transNo, userId, TransactionRecord.TYPE_EXPENSE,
                amount, bizType, bizId, key, now);
        try {
            transactionRecordMapper.customInsert(tableName, placeholder);
        } catch (DuplicateKeyException e) {
            TransactionRecord dup = transactionRecordMapper.customSelectByIdempotentKey(tableName, key);
            if (dup != null) {
                return toDto(dup);
            }
            throw e;
        }

        // ② 快速检查钱包状态（冻结则拒绝）
        UserWallet wallet = userWalletMapper.customSelectByUserId(userId);
        if (wallet != null && wallet.getStatus() != null && wallet.getStatus() != 1) {
            ResponseStatus.U_FREEZE.assertThrowResEx(true, "账户已被冻结，无法支出");
        }

        // ③ 原子支出：WHERE balance >= amount 防透支（先于限额，失败则回滚一切）
        int affected = userWalletMapper.customDeduct(userId, amount, now);
        if (affected == 0) {
            if (wallet == null || wallet.getBalance() < amount) {
                ResponseStatus.R_ACCOUNT_LESS_MONEY.assertThrowResEx(true);
            }
            ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(true);
        }

        // ④ 扣款成功后更新单日统计（失败不影响已成功的扣款——不在此处回滚，统计失败仅记录日志）
        String today = LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ISO_LOCAL_DATE);
        int statsAffected = userDailyStatsMapper.customAccumulate(userId, today, amount, DEFAULT_DAILY_LIMIT, now);
        if (statsAffected == 0) {
            try {
                userDailyStatsMapper.customInsert(userId, today, amount, now);
            } catch (DuplicateKeyException e) {
                statsAffected = userDailyStatsMapper.customAccumulate(userId, today, amount, DEFAULT_DAILY_LIMIT, now);
                if (statsAffected == 0) {
                    ResponseStatus.P_VALUE_ERROR.assertThrowResEx(true, "单日消费超限");
                }
            }
        }

        // ⑤ 读取实际余额 → 反写流水
        UserWallet walletAfter = userWalletMapper.customSelectByUserId(userId);
        long balanceAfter = walletAfter.getBalance();
        long balanceBefore = balanceAfter + amount;
        transactionRecordMapper.customUpdateBalances(tableName, id, balanceBefore, balanceAfter);

        placeholder.setBalanceBefore(balanceBefore);
        placeholder.setBalanceAfter(balanceAfter);
        return toDto(placeholder);
    }

    @Override
    @Transactional
    public TransactionDto refund(long userId, long originalTransId, String idempotentKey) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(originalTransId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(idempotentKey == null || idempotentKey.trim().isEmpty(), "幂等键不能为空");

        long now = System.currentTimeMillis();
        String key = idempotentKey.trim();
        String tableName = MonthTableRouter.tableName(TABLE_PREFIX, now);

        // ① 幂等检查（本月 + 上月）
        TransactionRecord existing = selectTransactionByIdempotentKey(key);
        if (existing != null) {
            return toDto(existing);
        }

        // ② 查原流水（本月 + 上月）
        TransactionRecord original = selectTransactionById(originalTransId);
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx(original == null, "原交易流水不存在");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(!original.getUserId().equals(userId), "非本人交易不可退款");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(original.getTransType() != TransactionRecord.TYPE_EXPENSE, "仅支出类型可退款");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(original.getStatus() != TransactionRecord.STATUS_SUCCESS, "该交易已退款或状态异常");

        long id = TRANS_ID_SNOWFLAKE.nextId();
        String transNo = allocateTransNo(id);

        // ③ INSERT 退款流水先占坑（写入当月）
        TransactionRecord refundRecord = TransactionRecord.builder()
                .id(id)
                .transNo(transNo)
                .userId(userId)
                .transType(TransactionRecord.TYPE_INCOME)
                .amount(original.getAmount())
                .balanceBefore(0L)
                .balanceAfter(0L)
                .bizType("refund")
                .bizId(String.valueOf(originalTransId))
                .idempotentKey(key)
                .refundId(originalTransId)
                .status(TransactionRecord.STATUS_SUCCESS)
                .remark("退款")
                .createTime(now)
                .build();

        try {
            transactionRecordMapper.customInsert(tableName, refundRecord);
        } catch (DuplicateKeyException e) {
            TransactionRecord dup = selectTransactionByIdempotentKey(key);
            if (dup != null) {
                return toDto(dup);
            }
            throw e;
        }

        // ④ 标记原流水已回退（需确认原流水所在月表）
        String originalTable = MonthTableRouter.tableName(TABLE_PREFIX, original.getCreateTime());
        int marked = transactionRecordMapper.customMarkReversed(originalTable, originalTransId);
        if (marked == 0) {
            ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(true, "标记原流水失败（可能已被退款）");
        }

        // ⑤ 原子加回余额
        int affected = userWalletMapper.customCredit(userId, original.getAmount(), now);
        if (affected == 0) {
            ensureWalletExists(userId, now);
            affected = userWalletMapper.customCredit(userId, original.getAmount(), now);
            if (affected == 0) {
                ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(true);
            }
        }

        // ⑥ 读取实际余额 → 反写退款流水
        UserWallet wallet = userWalletMapper.customSelectByUserId(userId);
        long balanceAfter = wallet.getBalance();
        long balanceBefore = balanceAfter - original.getAmount();
        transactionRecordMapper.customUpdateBalances(tableName, id, balanceBefore, balanceAfter);

        refundRecord.setBalanceBefore(balanceBefore);
        refundRecord.setBalanceAfter(balanceAfter);
        return toDto(refundRecord);
    }

    @Override
    public WalletDto getWallet(long userId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);

        UserWallet wallet = userWalletMapper.customSelectByUserId(userId);
        return toDto(wallet);
    }

    @Override
    public TransactionListDto getTransactions(long userId, long cursor, int limit) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);

        if (limit <= 0 || limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }

        // 固定 3 个月窗口，从当月往前扫
        List<String> months = MonthTableRouter.queryMonths(MonthTableRouter.currentMonth(), 3);
        List<TransactionRecord> allRecords = new ArrayList<>();

        for (String month : months) {
            String tbl = MonthTableRouter.tableName(TABLE_PREFIX, month);
            List<TransactionRecord> batch;
            try {
                batch = transactionRecordMapper.customSelectByUserId(tbl, userId, cursor, limit + 1 - allRecords.size());
            } catch (Exception e) {
                // 表不存在跳过
                continue;
            }
            if (batch != null) {
                allRecords.addAll(batch);
            }
            if (allRecords.size() >= limit + 1) {
                break;
            }
        }

        if (allRecords.isEmpty()) {
            return TransactionListDto.EMPTY;
        }

        boolean hasMore = allRecords.size() > limit;
        if (hasMore) {
            allRecords = allRecords.subList(0, limit);
        }

        long nextCursor = hasMore && !allRecords.isEmpty()
                ? allRecords.get(allRecords.size() - 1).getCreateTime()
                : 0L;

        return toListDto(allRecords, nextCursor, hasMore);
    }

    // ---- 跨月查询辅助方法 ----

    /** 本月 + 上月按幂等键查询 */
    private TransactionRecord selectTransactionByIdempotentKey(String key) {
        String current = MonthTableRouter.currentMonth();
        String currentTable = MonthTableRouter.tableName(TABLE_PREFIX, current);
        TransactionRecord record = transactionRecordMapper.customSelectByIdempotentKey(currentTable, key);
        if (record != null) return record;
        String lastMonth = MonthTableRouter.queryMonths(current, 2).get(1);
        String lastTable = MonthTableRouter.tableName(TABLE_PREFIX, lastMonth);
        try {
            return transactionRecordMapper.customSelectByIdempotentKey(lastTable, key);
        } catch (Exception e) {
            return null;
        }
    }

    /** 本月 + 上月按 ID 查询 */
    private TransactionRecord selectTransactionById(long id) {
        String current = MonthTableRouter.currentMonth();
        String currentTable = MonthTableRouter.tableName(TABLE_PREFIX, current);
        TransactionRecord record = transactionRecordMapper.customSelectById(currentTable, id);
        if (record != null) return record;
        String lastMonth = MonthTableRouter.queryMonths(current, 2).get(1);
        String lastTable = MonthTableRouter.tableName(TABLE_PREFIX, lastMonth);
        try {
            return transactionRecordMapper.customSelectById(lastTable, id);
        } catch (Exception e) {
            return null;
        }
    }

    // ---- 私有方法 ----

    private void validateParams(long userId, long amount, String bizType, String idempotentKey) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(amount <= 0, "金额必须大于0");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(bizType == null || bizType.trim().isEmpty(), "业务类型不能为空");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(idempotentKey == null || idempotentKey.trim().isEmpty(), "幂等键不能为空");
    }

    /** 构建占位流水（余额暂填0，INSERT 成功后 UPDATE 回填真实余额） */
    private TransactionRecord buildPlaceholder(long id, String transNo, long userId, int transType,
                                                long amount, String bizType, String bizId,
                                                String idempotentKey, long now) {
        return TransactionRecord.builder()
                .id(id)
                .transNo(transNo)
                .userId(userId)
                .transType(transType)
                .amount(amount)
                .balanceBefore(0L)
                .balanceAfter(0L)
                .bizType(bizType.trim())
                .bizId(defaultEmpty(bizId))
                .idempotentKey(idempotentKey)
                .status(TransactionRecord.STATUS_SUCCESS)
                .remark("")
                .createTime(now)
                .build();
    }

    /**
     * 确保用户钱包存在。不存在则创建（初始余额为 0）。
     * <p>
     * 并发安全：如果多个线程同时检测到钱包不存在并尝试创建，
     * 第一个 INSERT 成功，后续 INSERT 抛 DuplicateKeyException，捕获后继续执行。
     */
    private void ensureWalletExists(long userId, long now) {
        UserWallet wallet = userWalletMapper.customSelectByUserId(userId);
        if (wallet != null) {
            return;
        }
        try {
            userWalletMapper.customInsert(userId, now, now);
        } catch (DuplicateKeyException e) {
            // 其他并发请求已创建，忽略
        }
    }
}
