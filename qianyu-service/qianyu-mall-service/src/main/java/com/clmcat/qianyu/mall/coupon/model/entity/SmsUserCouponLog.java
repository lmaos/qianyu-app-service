package com.clmcat.qianyu.mall.coupon.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 优惠券使用记录（sms_user_coupon_log）。action: 1=领 2=用 3=回滚 4=过期。
 */
@Data
@Table("sms_user_coupon_log")
public class SmsUserCouponLog {

    @Id(keyType = KeyType.None)
    @Column(comment = "主键")
    private Long id;

    @Column(value = "user_coupon_id", comment = "关联 sms_user_coupon.id")
    private Long userCouponId;

    @Column(value = "user_id", comment = "用户 ID")
    private Long userId;

    @Column(value = "coupon_id", comment = "优惠券模板 ID")
    private Long couponId;

    @Column(value = "action", comment = "操作：1=领取 2=核销 3=回滚 4=过期")
    private Integer action;

    @Column(value = "order_id", comment = "关联订单 ID（USE/ROLLBACK 时有值）")
    private Long orderId;

    @Column(value = "order_no", comment = "订单编号（冗余）")
    private String orderNo;

    @Column(value = "remark", comment = "备注")
    private String remark;

    @Column(value = "create_time", comment = "操作时间（毫秒）")
    private Long createTime;
}
