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
 * 用户虚拟货币钱包。
 * <p>
 * 每个用户只有一条记录，以 user_id 为主键。
 * 余额变更通过原子 SQL（{@code SET balance = balance +/- ?}）操作，不在此实体中设值。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_wallet")
public class UserWallet {

    /** 用户ID（主键） */
    @Id(keyType = KeyType.None)
    @Column("user_id")
    private Long userId;

    /** 可用余额（最小单位） */
    @Column("balance")
    private Long balance;

    /** 冻结余额（下单未确认，不可用） */
    @Column("frozen_balance")
    private Long frozenBalance;

    /** 累计收入 */
    @Column("total_income")
    private Long totalIncome;

    /** 累计支出 */
    @Column("total_expense")
    private Long totalExpense;

    /** 乐观锁版本号 */
    @Column("version")
    private Integer version;

    /** 钱包状态：1=正常 2=冻结支出 3=冻结全部 */
    @Column("status")
    private Integer status;

    /** 创建时间戳（毫秒） */
    @Column("create_time")
    private Long createTime;

    /** 更新时间戳（毫秒） */
    @Column("update_time")
    private Long updateTime;
}
