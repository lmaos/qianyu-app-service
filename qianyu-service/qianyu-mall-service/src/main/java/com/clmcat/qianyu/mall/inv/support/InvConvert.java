package com.clmcat.qianyu.mall.inv.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;

/**
 * Snowflake generators are consolidated in {@link InvSupport}.
 * Constants here delegate to the canonical source for backward compatibility.
 */
public class InvConvert {

    public static final CustomSnowflake STOCK_ID_SNOWFLAKE = InvSupport.INV_ID_SNOWFLAKE;
    public static final CustomSnowflake STOCK_LOG_ID_SNOWFLAKE = InvSupport.STOCK_LOG_ID_SNOWFLAKE;
}
