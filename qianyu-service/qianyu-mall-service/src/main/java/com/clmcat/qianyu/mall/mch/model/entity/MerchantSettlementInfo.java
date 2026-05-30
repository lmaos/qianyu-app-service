package com.clmcat.qianyu.mall.mch.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("mch_settlement_info")
public class MerchantSettlementInfo {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "merchant_id", comment = "商家ID")
    private Long merchantId;

    @Column(value = "settlement_type", comment = "结算方式: 1=银行卡 2=支付宝 3=微信")
    private Integer settlementType;

    @Column(value = "account_name", comment = "账户持有人姓名")
    private String accountName;

    @Column(value = "account_no", comment = "账户号码（加密存储）")
    private String accountNo;

    @Column(value = "bank_name", comment = "开户银行名称")
    private String bankName;

    @Column(value = "bank_branch", comment = "开户支行名称")
    private String bankBranch;

    @Column(value = "bank_code", comment = "银行编码（联行号）")
    private String bankCode;

    @Column(value = "is_default", comment = "是否默认: 0=否 1=是")
    private Integer isDefault;

    @Column(value = "status", comment = "状态: 0=禁用 1=正常")
    private Integer status;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
