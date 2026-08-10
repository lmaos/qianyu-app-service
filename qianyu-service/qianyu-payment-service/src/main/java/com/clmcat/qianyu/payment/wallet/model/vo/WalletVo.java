package com.clmcat.qianyu.payment.wallet.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户钱包视图对象（返回前端）。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletVo {

    /** 用户ID */
    private Long userId;

    /** 可用余额（最小单位） */
    private Long balance;

    /** 累计收入 */
    private Long totalIncome;

    /** 累计支出 */
    private Long totalExpense;
}
