package com.clmcat.qianyu.mall.pay.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("pay_refund")
public class PayRefund {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "refund_no", comment = "退款单号")
    private String refundNo;

    @Column(value = "payment_id", comment = "关联支付记录 ID")
    private Long paymentId;

    @Column(value = "order_id", comment = "关联订单 ID")
    private Long orderId;

    @Column(value = "after_sale_id", comment = "关联售后单 ID")
    private Long afterSaleId;

    @Column(value = "amount", comment = "退款金额（元）")
    private BigDecimal amount;

    @Column(value = "reason", comment = "退款原因")
    private String reason;

    @Column(value = "refund_channel", comment = "退款渠道：1=原路退回 2=退到余额")
    private Integer refundChannel;

    @Column(value = "refund_status", comment = "退款状态：10=待退款 20=退款成功 30=退款失败")
    private Integer refundStatus;

    @Column(value = "transaction_id", comment = "第三方退款流水号")
    private String transactionId;

    @Column(value = "callback_data", comment = "第三方退款回调原始数据JSON", typeHandler = JacksonTypeHandler.class)
    private String callbackData;

    @Column(value = "refund_time", comment = "退款完成时间（毫秒时间戳）")
    private Long refundTime;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除：0=未删除 1=已删除", isLogicDelete = true)
    private Integer deleted;

    // ===== 退款状态常量 =====
    public static final int REFUND_STATUS_PENDING = 10;
    public static final int REFUND_STATUS_SUCCESS = 20;
    public static final int REFUND_STATUS_FAILED  = 30;

    // ===== 退款渠道常量 =====
    public static final int REFUND_CHANNEL_ORIGINAL = 1;
    public static final int REFUND_CHANNEL_BALANCE  = 2;
}
