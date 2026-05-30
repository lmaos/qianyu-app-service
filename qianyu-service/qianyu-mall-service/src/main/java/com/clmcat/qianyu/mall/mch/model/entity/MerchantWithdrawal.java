package com.clmcat.qianyu.mall.mch.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("mch_withdrawal")
public class MerchantWithdrawal {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "withdrawal_no", comment = "提现单号")
    private String withdrawalNo;

    @Column(value = "merchant_id", comment = "商家ID")
    private Long merchantId;

    @Column(value = "amount", comment = "提现金额（元）")
    private BigDecimal amount;

    @Column(value = "bank_name", comment = "收款银行名称")
    private String bankName;

    @Column(value = "bank_account", comment = "收款银行账号（脱敏存储）")
    private String bankAccount;

    @Column(value = "account_name", comment = "收款人姓名")
    private String accountName;

    @Column(value = "status", comment = "提现状态: 0=待审核 1=审核通过 2=打款中 3=打款成功 4=审核拒绝 5=打款失败")
    private Integer status;

    @Column(value = "reject_reason", comment = "拒绝原因")
    private String rejectReason;

    @Column(value = "transfer_no", comment = "打款流水号")
    private String transferNo;

    @Column(value = "transfer_time", comment = "打款成功时间（毫秒时间戳）")
    private Long transferTime;

    @Column(value = "create_time", comment = "申请时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;
}
