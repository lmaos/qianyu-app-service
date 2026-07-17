package com.clmcat.qianyu.mall.coupon.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import java.io.Serializable;

/**
 * Coupon 域状态常量 + 错误码（HTTP 200，业务码 412xxx）。
 */
public class CouponStatus implements ResponseErrorStatus, Serializable {

    // —— sms_coupon.status ——
    public static final int COUPON_DISABLED = 0;  // 禁用
    public static final int COUPON_ENABLED  = 1;  // 启用
    public static final int COUPON_EXPIRED  = 2;  // 已过期

    // —— sms_coupon.type ——
    public static final int TYPE_FULL_REDUCTION = 1; // 满减
    public static final int TYPE_DISCOUNT       = 2; // 折扣
    public static final int TYPE_FREE_SHIPPING  = 3; // 免运费

    // —— sms_user_coupon.status ——
    public static final int UC_UNUSED  = 1; // 未使用
    public static final int UC_USED    = 2; // 已使用
    public static final int UC_EXPIRED = 3; // 已过期

    // —— sms_user_coupon_log.action ——
    public static final int ACTION_CLAIM    = 1; // 领取
    public static final int ACTION_USE      = 2; // 核销
    public static final int ACTION_ROLLBACK = 3; // 回滚
    public static final int ACTION_EXPIRE   = 4; // 过期

    // ====== 错误码 ======
    public static final CouponStatus COUPON_NOT_FOUND          = new CouponStatus(412001, "优惠券不存在");
    public static final CouponStatus COUPON_NOT_USABLE         = new CouponStatus(412002, "优惠券不可用（已使用/已过期/未生效）");
    public static final CouponStatus COUPON_SOLD_OUT           = new CouponStatus(412003, "优惠券已领完");
    public static final CouponStatus COUPON_LIMIT_EXCEED       = new CouponStatus(412004, "已超过每人限领数量");
    public static final CouponStatus COUPON_NOT_BELONG_USER    = new CouponStatus(412005, "优惠券不属于当前用户");
    public static final CouponStatus COUPON_THRESHOLD_NOT_MET  = new CouponStatus(412006, "未达到优惠券使用门槛");
    public static final CouponStatus COUPON_SCOPE_NOT_MATCH    = new CouponStatus(412007, "优惠券不适用于当前商品");
    public static final CouponStatus COUPON_NOT_BELONG_MERCHANT= new CouponStatus(412008, "优惠券不属于当前商家");

    private final int httpStatus = 200;
    private final Integer status;
    private final String message;
    private final String describe;

    private CouponStatus(Integer status, String message) {
        this.status = status;
        this.message = message;
        this.describe = message;
    }

    @Override public int getHttpStatus() { return httpStatus; }
    @Override public String getState() { return name(); }
    @Override public Integer getStatus() { return status; }
    @Override public String getMessage() { return message; }

    public String name() {
        return "COUPON_" + status;
    }
}
