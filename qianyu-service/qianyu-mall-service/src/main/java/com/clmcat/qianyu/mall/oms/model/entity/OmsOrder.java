package com.clmcat.qianyu.mall.oms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("oms_order")
public class OmsOrder {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "order_no", comment = "订单编号")
    private String orderNo;

    @Column(value = "user_id", comment = "买家用户 ID")
    private Long userId;

    @Column(value = "merchant_id", comment = "商家 ID")
    private Long merchantId;

    @Column(value = "total_amount", comment = "订单总金额（元）")
    private BigDecimal totalAmount;

    @Column(value = "pay_amount", comment = "实付金额（元）")
    private BigDecimal payAmount;

    @Column(value = "freight_amount", comment = "运费金额（元）")
    private BigDecimal freightAmount;

    @Column(value = "coupon_amount", comment = "优惠券抵扣金额（元）")
    private BigDecimal couponAmount;

    @Column(value = "coupon_user_id", comment = "使用的用户优惠券ID")
    private Long couponUserId;

    @Column(value = "discount_amount", comment = "促销优惠金额（元）")
    private BigDecimal discountAmount;

    @Column(value = "discount_detail", comment = "优惠明细JSON")
    private String discountDetail;

    @Column(value = "total_quantity", comment = "订单总商品件数")
    private Integer totalQuantity;

    @Column(value = "status", comment = "订单状态: 10/20/30/40/50/60")
    private Integer status;

    @Column(value = "after_sale_status", comment = "售后状态: 0=无售后 1=售后中 2=售后成功 3=售后拒绝/取消")
    private Integer afterSaleStatus;

    @Column(value = "after_sale_type", comment = "售后类型: 0=无 1=仅退款 2=退货退款 3=换货 4=维修")
    private Integer afterSaleType;

    @Column(value = "version", comment = "乐观锁版本号")
    private Long version;

    @Column(value = "source", comment = "订单来源：1=APP 2=H5 3=微信小程序 4=直播间")
    private Integer source;

    @Column(value = "buyer_ip", comment = "下单客户端IP")
    private String buyerIp;

    @Column(value = "buyer_message", comment = "买家留言")
    private String buyerMessage;

    @Column(value = "merchant_remark", comment = "商家内部备注")
    private String merchantRemark;

    @Column(value = "address_snapshot", comment = "收货地址快照JSON")
    private String addressSnapshot;

    @Column(value = "pay_time", comment = "支付时间（毫秒时间戳）")
    private Long payTime;

    @Column(value = "delivery_time", comment = "发货时间（毫秒时间戳）")
    private Long deliveryTime;

    @Column(value = "receive_time", comment = "确认收货时间（毫秒时间戳）")
    private Long receiveTime;

    @Column(value = "close_time", comment = "关闭时间（毫秒时间戳）")
    private Long closeTime;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除, 1=已删除", isLogicDelete = true)
    private Integer deleted;

    // ===== 订单状态常量 =====
    public static final int STATUS_PENDING_PAY    = 10;
    public static final int STATUS_PENDING_SHIP   = 20;
    public static final int STATUS_SHIPPED        = 30;
    public static final int STATUS_COMPLETED      = 40;
    public static final int STATUS_CANCELLED      = 50;
    public static final int STATUS_CLOSED         = 60;
}
