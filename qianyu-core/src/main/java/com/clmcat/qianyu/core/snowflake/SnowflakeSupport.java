package com.clmcat.qianyu.core.snowflake;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.basics.commons.snowflake.SnowflakeCustomBuilder;
import com.clmcat.basics.commons.snowflake.strategy.MachineStrategy;
import com.clmcat.basics.commons.snowflake.strategy.SequenceStrategy;
import com.clmcat.basics.commons.snowflake.strategy.TimeStrategy;

public class SnowflakeSupport {
    /**
     * 雪花算法基准时间， 项目上线后不可再修改
     */
    public static final long BASE_TIME = 1779465600000L;


    public static CustomSnowflake createSnowflake(int timeStrategyBit, int machineStrategyBit, int sequenceStrategyBit) {
        return SnowflakeCustomBuilder.builder()
                .add(0) // 首位 0
                .add("TimeStrategy", timeStrategyBit, TimeStrategy.millisecond(BASE_TIME)) // 时间
                .add("MachineStrategy", machineStrategyBit, MachineStrategy.autoByIp()) // 机器ID
                .add("SequenceStrategy", sequenceStrategyBit, // 计数器
                        SequenceStrategy.groupedWindow(5000), "TimeStrategy")
                .build();
    }

    /**
     * 根据雪花算法解析登录用户ID中的时间戳
     * @param id 雪花ID
     * @return 时间戳
     */
    public static long parseTimeBySnowflake(CustomSnowflake snowflake, long id) {
        return snowflake.get("TimeStrategy", id) + BASE_TIME;
    }
}
