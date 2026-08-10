package com.clmcat.qianyu.payment.wallet.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易流水记录。
 * <p>
 * 每笔余额变动都对应一条流水，用于审计和追溯。
 * {@code idempotentKey} 上有唯一索引，保证同一业务操作不会被重复处理。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("transaction_record")
public class TransactionRecord {

    /** 流水ID（雪花） */
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;

    /** 统一交易流水号 */
    @Column("trans_no")
    private String transNo;

    /** 用户ID */
    @Column("user_id")
    private Long userId;

    /** 交易类型：1=收入 2=支出 */
    @Column("trans_type")
    private Integer transType;

    /** 交易金额（最小单位） */
    @Column("amount")
    private Long amount;

    /** 交易前余额 */
    @Column("balance_before")
    private Long balanceBefore;

    /** 交易后余额 */
    @Column("balance_after")
    private Long balanceAfter;

    /** 业务类型 */
    @Column("biz_type")
    private String bizType;

    /** 业务单号 */
    @Column("biz_id")
    private String bizId;

    /** 幂等键（唯一索引） */
    @Column("idempotent_key")
    private String idempotentKey;

    /** 状态：1=成功 2=已回退 */
    @Column("status")
    private Integer status;

    /** 对手方用户ID */
    @Column("counterparty_user_id")
    private Long counterpartyUserId;

    /** 关联流水ID */
    @Column("correlation_id")
    private Long correlationId;

    /** 退款关联原流水ID */
    @Column("refund_id")
    private Long refundId;

    /** 备注 */
    @Column("remark")
    private String remark;

    /** 创建时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    // ===== 常量 =====
    public static final int TYPE_INCOME  = 1;
    public static final int TYPE_EXPENSE = 2;

    public static final int STATUS_SUCCESS  = 1;
    public static final int STATUS_REVERSED = 2;
}
