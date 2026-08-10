package com.clmcat.qianyu.payment.settlement.service;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.core.table.MonthTableRouter;
import com.clmcat.qianyu.payment.api.settlement.SettlementApi;
import com.clmcat.qianyu.payment.api.settlement.model.dto.SettlementDto;
import com.clmcat.qianyu.payment.api.settlement.model.dto.SettlementRecordDto;
import com.clmcat.qianyu.payment.api.settlement.model.dto.SettlementRecordListDto;
import com.clmcat.qianyu.payment.settlement.mapper.HostSettlementMapper;
import com.clmcat.qianyu.payment.settlement.mapper.SettlementRecordMapper;
import com.clmcat.qianyu.payment.settlement.model.entity.HostSettlement;
import com.clmcat.qianyu.payment.settlement.model.entity.SettlementRecord;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 结算货币核心服务。
 * <p>
 * 仅通过交易订单（trade_order）产生结算收入，不对前端暴露收入接口。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@DubboService
@Service
public class SettlementServiceBiz implements SettlementApi {

    private static final CustomSnowflake SETTLE_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    private static final int MAX_LIMIT = 50;
    private static final String TABLE_PREFIX = "settlement_record";

    @Resource
    private HostSettlementMapper hostSettlementMapper;

    @Resource
    private SettlementRecordMapper settlementRecordMapper;

    @Override
    @Transactional
    public SettlementRecordDto credit(long userId, long amount, int settleType, String transNo, String bizNo,
                                       int commissionRate, String idempotentKey) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(amount <= 0, "金额必须大于0");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(transNo == null || transNo.trim().isEmpty(), "消费流水号不能为空");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(bizNo == null || bizNo.trim().isEmpty(), "业务流水号不能为空");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(idempotentKey == null || idempotentKey.trim().isEmpty(), "幂等键不能为空");

        String key = idempotentKey.trim();

        long now = System.currentTimeMillis();
        long id = SETTLE_ID_SNOWFLAKE.nextId();
        String tableName = MonthTableRouter.tableName(TABLE_PREFIX, now);

        // ① INSERT 流水先占坑
        SettlementRecord record = SettlementRecord.builder()
                .id(id)
                .transNo(transNo.trim())
                .bizNo(bizNo.trim())
                .userId(userId)
                .settleType(settleType)
                .amount(amount)
                .balanceBefore(0L)
                .balanceAfter(0L)
                .commissionRate(commissionRate)
                .idempotentKey(key)
                .status(SettlementRecord.STATUS_SUCCESS)
                .remark("")
                .createTime(now)
                .build();

        try {
            settlementRecordMapper.customInsert(tableName, record);
        } catch (DuplicateKeyException e) {
            SettlementRecord dup = settlementRecordMapper.customSelectByIdempotentKey(tableName, key);
            if (dup != null) {
                return toDto(dup);
            }
            throw e;
        }

        // ② 确保结算账户存在
        ensureSettlementExists(userId, now);

        // ③ 原子收入
        int affected = hostSettlementMapper.customCredit(userId, amount, now);
        if (affected == 0) {
            ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(true);
        }

        // ④ 读取余额 → 反写流水
        HostSettlement settlement = hostSettlementMapper.customSelectByUserId(userId);
        long balanceAfter = settlement.getBalance();
        long balanceBefore = balanceAfter - amount;
        settlementRecordMapper.customUpdateBalances(tableName, id, balanceBefore, balanceAfter);

        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        return toDto(record);
    }

    @Override
    public SettlementDto getSettlement(long userId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        HostSettlement s = hostSettlementMapper.customSelectByUserId(userId);
        return toDto(s);
    }

    @Override
    public SettlementRecordListDto getRecords(long userId, long cursor, int limit) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        if (limit <= 0 || limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }

        // 固定 3 个月窗口，从当月往前扫
        List<String> months = MonthTableRouter.queryMonths(MonthTableRouter.currentMonth(), 3);
        List<SettlementRecord> allRecords = new ArrayList<>();

        for (String month : months) {
            String tbl = MonthTableRouter.tableName(TABLE_PREFIX, month);
            List<SettlementRecord> batch;
            try {
                batch = settlementRecordMapper.customSelectByUserId(tbl, userId, cursor, limit + 1 - allRecords.size());
            } catch (Exception e) {
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
            return SettlementRecordListDto.EMPTY;
        }

        boolean hasMore = allRecords.size() > limit;
        if (hasMore) {
            allRecords = allRecords.subList(0, limit);
        }

        long nextCursor = hasMore && !allRecords.isEmpty() ? allRecords.get(allRecords.size() - 1).getId() : 0L;
        List<SettlementRecordDto> dtos = allRecords.stream().map(SettlementServiceBiz::toDto).collect(Collectors.toList());
        return SettlementRecordListDto.builder()
                .records(dtos)
                .cursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    private void ensureSettlementExists(long userId, long now) {
        HostSettlement s = hostSettlementMapper.customSelectByUserId(userId);
        if (s != null) return;
        try {
            hostSettlementMapper.customInsert(userId, now, now);
        } catch (DuplicateKeyException e) {
            // 并发创建，忽略
        }
    }

    public static SettlementDto toDto(HostSettlement s) {
        if (s == null) return null;
        return SettlementDto.builder()
                .userId(s.getUserId())
                .balance(s.getBalance())
                .totalEarning(s.getTotalEarning())
                .frozenBalance(s.getFrozenBalance())
                .status(s.getStatus())
                .build();
    }

    public static SettlementRecordDto toDto(SettlementRecord r) {
        if (r == null) return null;
        return SettlementRecordDto.builder()
                .id(r.getId())
                .transNo(r.getTransNo())
                .bizNo(r.getBizNo())
                .userId(r.getUserId())
                .settleType(r.getSettleType())
                .amount(r.getAmount())
                .balanceBefore(r.getBalanceBefore())
                .balanceAfter(r.getBalanceAfter())
                .commissionRate(r.getCommissionRate())
                .status(r.getStatus())
                .createTime(r.getCreateTime())
                .build();
    }
}
