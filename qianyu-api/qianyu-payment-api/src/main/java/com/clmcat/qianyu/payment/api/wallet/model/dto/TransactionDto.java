package com.clmcat.qianyu.payment.api.wallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 交易流水信息（RPC 返回）。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto implements Serializable {

    /** 流水ID（雪花） */
    private Long id;

    /** 统一交易流水号 */
    private String transNo;

    /** 用户ID */
    private Long userId;

    /** 交易类型：1=收入 2=支出 */
    private Integer transType;

    /** 交易金额（最小单位） */
    private Long amount;

    /** 交易前余额 */
    private Long balanceBefore;

    /** 交易后余额 */
    private Long balanceAfter;

    /** 业务类型 */
    private String bizType;

    /** 业务单号 */
    private String bizId;

    /** 幂等键 */
    private String idempotentKey;

    /** 状态：1=成功 2=已回退 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间戳（毫秒） */
    private Long createTime;

    // ===== 交易类型常量 =====
    public static final int TYPE_INCOME  = 1;
    public static final int TYPE_EXPENSE = 2;

    // ===== 状态常量 =====
    public static final int STATUS_SUCCESS  = 1;
    public static final int STATUS_REVERSED = 2;
}
