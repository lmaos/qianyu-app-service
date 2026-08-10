package com.clmcat.qianyu.payment.wallet.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 钱包操作参数（收入/支出）。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@NoArgsConstructor
public class WalletOperateDto {

    /** 金额（最小单位） */
    private Long amount;

    /** 业务类型 */
    private String bizType;

    /** 业务单号 */
    private String bizId;

    /** 幂等键（全局唯一） */
    private String idempotentKey;
}
