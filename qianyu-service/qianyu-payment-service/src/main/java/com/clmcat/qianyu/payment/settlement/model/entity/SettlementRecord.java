package com.clmcat.qianyu.payment.settlement.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 结算流水记录。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("settlement_record")
public class SettlementRecord {

    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;

    @Column("trans_no")
    private String transNo;

    @Column("biz_no")
    private String bizNo;

    @Column("user_id")
    private Long userId;

    @Column("settle_type")
    private Integer settleType;

    @Column("amount")
    private Long amount;

    @Column("balance_before")
    private Long balanceBefore;

    @Column("balance_after")
    private Long balanceAfter;

    @Column("commission_rate")
    private Integer commissionRate;

    @Column("idempotent_key")
    private String idempotentKey;

    @Column("status")
    private Integer status;

    @Column("remark")
    private String remark;

    @Column("create_time")
    private Long createTime;

    public static final int TYPE_GIFT     = 1;
    public static final int TYPE_LIVE     = 2;
    public static final int TYPE_ACTIVITY = 3;
    public static final int TYPE_REFUND   = 4;

    public static final int STATUS_SUCCESS  = 1;
    public static final int STATUS_REVERSED = 2;
}
