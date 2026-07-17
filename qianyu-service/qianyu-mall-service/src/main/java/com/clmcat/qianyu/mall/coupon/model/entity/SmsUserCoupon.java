package com.clmcat.qianyu.mall.coupon.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 用户优惠券（sms_user_coupon）。status: 1=未使用 2=已使用 3=已过期。
 */
@Data
@Table("sms_user_coupon")
public class SmsUserCoupon {

    @Id(keyType = KeyType.None)
    @Column(comment = "主键")
    private Long id;

    @Column(value = "coupon_id", comment = "优惠券模板 ID")
    private Long couponId;

    @Column(value = "source_type", comment = "来源：1=主动领取 2=系统发放 3=活动 4=邀请")
    private Integer sourceType;

    @Column(value = "user_id", comment = "领取用户 ID")
    private Long userId;

    @Column(value = "order_id", comment = "核销订单 ID（nullable）")
    private Long orderId;

    @Column(value = "status", comment = "状态：1=未使用 2=已使用 3=已过期")
    private Integer status;

    @Column(value = "use_time", comment = "使用时间（毫秒，nullable）")
    private Long useTime;

    @Column(value = "expire_time", comment = "过期时间（毫秒）")
    private Long expireTime;

    @Column(value = "create_time", comment = "创建时间（毫秒）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除")
    private Integer deleted;
}
