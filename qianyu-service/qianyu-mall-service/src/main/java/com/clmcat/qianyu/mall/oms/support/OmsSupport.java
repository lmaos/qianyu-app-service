package com.clmcat.qianyu.mall.oms.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;

public class OmsSupport {

    public static final CustomSnowflake CART_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake ORDER_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake ORDER_ITEM_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake AFTERSALE_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
}
