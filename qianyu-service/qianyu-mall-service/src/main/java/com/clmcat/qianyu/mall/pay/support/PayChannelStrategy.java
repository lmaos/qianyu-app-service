package com.clmcat.qianyu.mall.pay.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;

public class PayChannelStrategy {

    public static final CustomSnowflake PAYMENT_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake REFUND_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
}
