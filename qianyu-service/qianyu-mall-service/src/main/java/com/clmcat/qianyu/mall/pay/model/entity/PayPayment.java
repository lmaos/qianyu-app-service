package com.clmcat.qianyu.mall.pay.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("pay_payment")
public class PayPayment {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "payment_no", comment = "支付单号")
    private String paymentNo;

    @Column(value = "order_id", comment = "关联订单 ID")
    private Long orderId;

    @Column(value = "user_id", comment = "支付用户 ID")
    private Long userId;

    @Column(value = "merchant_id", comment = "收款商家ID")
    private Long merchantId;

    @Column(value = "amount", comment = "支付金额（元）")
    private BigDecimal amount;

    @Column(value = "pay_channel", comment = "支付渠道：1=微信支付 2=支付宝 3=余额支付")
    private Integer payChannel;

    @Column(value = "pay_type", comment = "支付方式：1=微信JSAPI 2=微信APP 3=微信H5 4=微信Native 5=支付宝APP 6=支付宝H5 7=支付宝网页 8=余额支付")
    private Integer payType;

    @Column(value = "third_pay_uid", comment = "第三方支付用户标识")
    private String thirdPayUid;

    @Column(value = "buyer_ip", comment = "支付客户端IP")
    private String buyerIp;

    @Column(value = "pay_status", comment = "支付状态：10=待支付 20=支付成功 30=支付失败 40=已关闭")
    private Integer payStatus;

    @Column(value = "transaction_id", comment = "第三方交易号")
    private String transactionId;

    @Column(value = "callback_status", comment = "回调处理状态：0=未收到 1=已处理 2=处理失败")
    private Integer callbackStatus;

    @Column(value = "callback_data", comment = "第三方回调原始数据JSON")
    private String callbackData;

    @Column(value = "pay_time", comment = "支付成功时间（毫秒时间戳）")
    private Long payTime;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除：0=未删除 1=已删除", isLogicDelete = true)
    private Integer deleted;

    // ===== 支付状态常量 =====
    public static final int PAY_STATUS_PENDING  = 10;
    public static final int PAY_STATUS_SUCCESS  = 20;
    public static final int PAY_STATUS_FAILED   = 30;
    public static final int PAY_STATUS_CLOSED   = 40;

    // ===== 支付渠道常量 =====
    public static final int CHANNEL_WECHAT  = 1;
    public static final int CHANNEL_ALIPAY  = 2;
    public static final int CHANNEL_BALANCE = 3;

    // ===== 支付方式常量 =====
    public static final int TYPE_WECHAT_JSAPI = 1;
    public static final int TYPE_WECHAT_APP   = 2;
    public static final int TYPE_WECHAT_H5    = 3;
    public static final int TYPE_WECHAT_NATIVE = 4;
    public static final int TYPE_ALIPAY_APP   = 5;
    public static final int TYPE_ALIPAY_H5    = 6;
    public static final int TYPE_ALIPAY_PAGE  = 7;
    public static final int TYPE_BALANCE      = 8;

    // ===== 回调状态常量 =====
    public static final int CALLBACK_STATUS_NONE    = 0;
    public static final int CALLBACK_STATUS_DONE    = 1;
    public static final int CALLBACK_STATUS_FAILED  = 2;
}
