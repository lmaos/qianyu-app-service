package com.clmcat.qianyu.mall.oms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("oms_after_sale")
public class OmsAfterSale {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "after_sale_no", comment = "售后单号")
    private String afterSaleNo;

    @Column(value = "order_id", comment = "关联订单 ID")
    private Long orderId;

    @Column(value = "order_item_id", comment = "关联订单明细 ID")
    private Long orderItemId;

    @Column(value = "user_id", comment = "申请人用户 ID")
    private Long userId;

    @Column(value = "merchant_id", comment = "商家 ID")
    private Long merchantId;

    @Column(value = "type", comment = "售后类型: 1=仅退款 2=退货退款 3=换货 4=维修")
    private Integer type;

    @Column(value = "reason", comment = "售后原因")
    private String reason;

    @Column(value = "description", comment = "问题描述")
    private String description;

    @Column(value = "amount", comment = "退款金额（元）")
    private BigDecimal amount;

    @Column(value = "images", comment = "凭证图片列表JSON", typeHandler = JacksonTypeHandler.class)
    private String images;

    @Column(value = "status", comment = "售后状态: 10/20/30/40/50/60")
    private Integer status;

    @Column(value = "reject_reason", comment = "商家拒绝原因")
    private String rejectReason;

    @Column(value = "return_shipping_no", comment = "退货物流单号")
    private String returnShippingNo;

    @Column(value = "return_shipping_company", comment = "退货物流公司编码")
    private String returnShippingCompany;

    @Column(value = "send_back_shipping_no", comment = "商家寄回物流单号")
    private String sendBackShippingNo;

    @Column(value = "send_back_shipping_company", comment = "商家寄回物流公司编码")
    private String sendBackShippingCompany;

    @Column(value = "refund_time", comment = "退款完成时间（毫秒时间戳）")
    private Long refundTime;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除, 1=已删除", isLogicDelete = true)
    private Integer deleted;

    // ===== 售后状态常量 =====
    public static final int STATUS_PENDING_REVIEW  = 10;
    public static final int STATUS_MERCHANT_AGREE  = 20;
    public static final int STATUS_MERCHANT_REJECT = 30;
    public static final int STATUS_USER_SHIPPED    = 40;
    public static final int STATUS_COMPLETED       = 50;
    public static final int STATUS_CANCELLED       = 60;

    // ===== 售后类型常量 =====
    public static final int TYPE_REFUND_ONLY       = 1;
    public static final int TYPE_RETURN_REFUND     = 2;
    public static final int TYPE_EXCHANGE          = 3;
    public static final int TYPE_REPAIR            = 4;
}
