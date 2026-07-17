package com.clmcat.qianyu.mall.coupon.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;

/**
 * Coupon 域 Snowflake ID 生成器（worker 50 避免与 inv 42 冲突）。
 */
public class CouponSupport {

    public static final CustomSnowflake COUPON_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake USER_COUPON_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake COUPON_LOG_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
}
