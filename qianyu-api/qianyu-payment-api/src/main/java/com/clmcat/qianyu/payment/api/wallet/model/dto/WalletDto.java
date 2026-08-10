package com.clmcat.qianyu.payment.api.wallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户钱包信息（RPC 返回）。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto implements Serializable {

    /** 用户ID */
    private Long userId;

    /** 可用余额（最小单位） */
    private Long balance;

    /** 冻结余额（最小单位） */
    private Long frozenBalance;

    /** 累计收入 */
    private Long totalIncome;

    /** 累计支出 */
    private Long totalExpense;
}
