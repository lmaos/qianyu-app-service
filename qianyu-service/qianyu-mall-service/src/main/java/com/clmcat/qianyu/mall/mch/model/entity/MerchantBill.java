package com.clmcat.qianyu.mall.mch.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("mch_bill")
public class MerchantBill {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "merchant_id", comment = "商家ID")
    private Long merchantId;

    @Column(value = "order_id", comment = "关联订单ID")
    private Long orderId;

    @Column(value = "order_no", comment = "订单编号")
    private String orderNo;

    @Column(value = "type", comment = "账单类型: 1=订单收入 2=退款支出 3=佣金调整")
    private Integer type;

    @Column(value = "order_amount", comment = "订单金额（元）")
    private BigDecimal orderAmount;

    @Column(value = "refund_amount", comment = "退款金额（元）")
    private BigDecimal refundAmount;

    @Column(value = "platform_fee", comment = "平台佣金（元）")
    private BigDecimal platformFee;

    @Column(value = "platform_rate", comment = "平台佣金比例（%）")
    private BigDecimal platformRate;

    @Column(value = "anchor_fee", comment = "主播佣金（元）")
    private BigDecimal anchorFee;

    @Column(value = "anchor_rate", comment = "主播佣金比例（%）")
    private BigDecimal anchorRate;

    @Column(value = "merchant_income", comment = "商家实际入账金额（元）")
    private BigDecimal merchantIncome;

    @Column(value = "settlement_id", comment = "关联结算单ID（0=未结算）")
    private Long settlementId;

    @Column(value = "status", comment = "结算状态: 0=未结算 1=已结算")
    private Integer status;

    @Column(value = "remark", comment = "备注")
    private String remark;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;
}
