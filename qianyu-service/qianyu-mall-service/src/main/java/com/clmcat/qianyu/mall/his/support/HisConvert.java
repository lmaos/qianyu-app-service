package com.clmcat.qianyu.mall.his.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;

public class HisConvert {

    public static final CustomSnowflake HIS_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.doubleValue() <= 0;
    }
}
