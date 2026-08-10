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
 * 主播结算账户。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("host_settlement")
public class HostSettlement {

    @Id(keyType = KeyType.None)
    @Column("user_id")
    private Long userId;

    @Column("balance")
    private Long balance;

    @Column("total_earning")
    private Long totalEarning;

    @Column("frozen_balance")
    private Long frozenBalance;

    @Column("status")
    private Integer status;

    @Column("version")
    private Integer version;

    @Column("create_time")
    private Long createTime;

    @Column("update_time")
    private Long updateTime;

    public static final int STATUS_NORMAL  = 1;
    public static final int STATUS_FROZEN  = 2;
}
