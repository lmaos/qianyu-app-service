package com.clmcat.qianyu.mall.inv.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;

public class InvSupport {
    public static final CustomSnowflake INV_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake STOCK_LOG_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.doubleValue() <= 0;
    }
}
