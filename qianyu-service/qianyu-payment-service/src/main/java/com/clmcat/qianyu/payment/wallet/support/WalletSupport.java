package com.clmcat.qianyu.payment.wallet.support;

import com.clmcat.qianyu.payment.api.wallet.model.dto.TransactionDto;
import com.clmcat.qianyu.payment.api.wallet.model.dto.TransactionListDto;
import com.clmcat.qianyu.payment.api.wallet.model.dto.WalletDto;
import com.clmcat.qianyu.payment.wallet.model.entity.TransactionRecord;
import com.clmcat.qianyu.payment.wallet.model.entity.UserWallet;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 钱包工具支持类。
 * <p>
 * 提供 Entity ↔ Dto ↔ Vo 转换、流水号分配钩子。
 *
 * @author ark-home
 * @date 2026-08-03
 */
public class WalletSupport {

    /**
     * 分配对外交易流水号（String 类型）。
     * 预留钩子方法，后续可覆写为业务前缀 + Snowflake 等生成逻辑。
     *
     * @param id 内部雪花ID
     * @return 交易流水号
     */
    protected String allocateTransNo(long id) {
        return String.valueOf(id);
    }

    // ---- 转换方法 ----

    /** UserWallet → WalletDto */
    public static WalletDto toDto(UserWallet wallet) {
        if (wallet == null) {
            return null;
        }
        return WalletDto.builder()
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .frozenBalance(wallet.getFrozenBalance())
                .totalIncome(wallet.getTotalIncome())
                .totalExpense(wallet.getTotalExpense())
                .build();
    }

    /** TransactionRecord → TransactionDto */
    public static TransactionDto toDto(TransactionRecord record) {
        if (record == null) {
            return null;
        }
        return TransactionDto.builder()
                .id(record.getId())
                .transNo(record.getTransNo())
                .userId(record.getUserId())
                .transType(record.getTransType())
                .amount(record.getAmount())
                .balanceBefore(record.getBalanceBefore())
                .balanceAfter(record.getBalanceAfter())
                .bizType(record.getBizType())
                .bizId(record.getBizId())
                .idempotentKey(record.getIdempotentKey())
                .status(record.getStatus())
                .remark(record.getRemark())
                .createTime(record.getCreateTime())
                .build();
    }

    /** List<TransactionRecord> → TransactionListDto */
    public static TransactionListDto toListDto(List<TransactionRecord> records, long cursor, boolean hasMore) {
        if (records == null || records.isEmpty()) {
            return TransactionListDto.EMPTY;
        }
        List<TransactionDto> dtos = records.stream()
                .map(WalletSupport::toDto)
                .collect(Collectors.toList());
        return TransactionListDto.builder()
                .transactions(dtos)
                .cursor(cursor)
                .hasMore(hasMore)
                .build();
    }

    /** 安全取字符串默认值 */
    protected static String defaultEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
