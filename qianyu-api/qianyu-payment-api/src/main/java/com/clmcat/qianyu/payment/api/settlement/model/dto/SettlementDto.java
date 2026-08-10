package com.clmcat.qianyu.payment.api.settlement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 主播结算账户信息（RPC 返回）。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementDto implements Serializable {

    /** 主播用户ID */
    private Long userId;

    /** 可结算余额 */
    private Long balance;

    /** 累计收益 */
    private Long totalEarning;

    /** 冻结中（提现审核中） */
    private Long frozenBalance;

    /** 状态：1=正常 2=冻结提现 */
    private Integer status;
}
