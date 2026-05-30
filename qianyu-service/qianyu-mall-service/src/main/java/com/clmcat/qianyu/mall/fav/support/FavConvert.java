package com.clmcat.qianyu.mall.fav.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;

public class FavConvert {

    public static final CustomSnowflake FAV_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.doubleValue() <= 0;
    }
}
