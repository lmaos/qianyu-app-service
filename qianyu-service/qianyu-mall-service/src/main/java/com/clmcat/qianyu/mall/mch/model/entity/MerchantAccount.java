package com.clmcat.qianyu.mall.mch.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("mch_account")
public class MerchantAccount {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "merchant_id", comment = "商家ID（唯一）")
    private Long merchantId;

    @Column(value = "balance", comment = "可用余额（元）")
    private BigDecimal balance;

    @Column(value = "frozen_amount", comment = "冻结金额（元）")
    private BigDecimal frozenAmount;

    @Column(value = "total_income", comment = "累计总收入（元）")
    private BigDecimal totalIncome;

    @Column(value = "total_withdraw", comment = "累计已提现（元）")
    private BigDecimal totalWithdraw;

    @Column(value = "total_refund", comment = "累计退款支出（元）")
    private BigDecimal totalRefund;

    @Column(value = "total_commission", comment = "累计平台佣金（元）")
    private BigDecimal totalCommission;

    @Column(value = "version", comment = "乐观锁版本号")
    private Long version;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;
}
