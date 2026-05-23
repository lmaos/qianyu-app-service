package com.clmcat.qianyu.user.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.basics.commons.snowflake.SnowflakeCustomBuilder;
import com.clmcat.basics.commons.snowflake.strategy.MachineStrategy;
import com.clmcat.basics.commons.snowflake.strategy.SequenceStrategy;
import com.clmcat.basics.commons.snowflake.strategy.TimeStrategy;

public class LoginSupport {

    public static final long BASE_TIME = 1779465600000L;

    public static final CustomSnowflake LOGIN_USER_ID_SNOWFLAKE = SnowflakeCustomBuilder.builder()
            .add(0) // 首位 0
            .add("TimeStrategy", 42, TimeStrategy.millisecond(BASE_TIME))
            .add("MachineStrategy", 10, MachineStrategy.autoByIp())
            .add("SequenceStrategy", 11, SequenceStrategy.create(), "TimeStrategy")
            .build();

    /**
     * 根据雪花算法解析登录用户ID中的时间戳
     * @param userId 用户ID
     * @return 时间戳
     */
    public static long parseTimeBySnowflake(long userId) {
        return LOGIN_USER_ID_SNOWFLAKE.get("TimeStrategy", userId);
    }

    public static long allocUserId() {
        return LOGIN_USER_ID_SNOWFLAKE.nextId();
    }

}
