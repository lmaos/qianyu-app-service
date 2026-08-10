package com.clmcat.qianyu.payment.wallet.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * user_daily_stats 表 Mapper。
 * <p>
 * 单日消费统计，用于累计限额。主键为 (user_id, stat_date)。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Mapper
public interface UserDailyStatsMapper {

    /**
     * 创建当日统计记录（首次消费时调用）。
     */
    @Insert("INSERT INTO user_daily_stats (user_id, stat_date, daily_expense, daily_count, version, create_time, update_time) " +
            "VALUES (#{userId}, #{statDate}, #{amount}, 1, 0, #{now}, #{now})")
    int customInsert(@Param("userId") long userId,
                     @Param("statDate") String statDate,
                     @Param("amount") long amount,
                     @Param("now") long now);

    /**
     * 原子累加当日支出。
     * WHERE daily_expense + amount <= dailyLimit 防止超限。
     * 返回受影响行数，0 表示超限或日期记录不存在。
     */
    @Update("UPDATE user_daily_stats SET daily_expense = daily_expense + #{amount}, " +
            "daily_count = daily_count + 1, " +
            "version = version + 1, " +
            "update_time = #{now} " +
            "WHERE user_id = #{userId} AND stat_date = #{statDate} " +
            "AND daily_expense + #{amount} <= #{dailyLimit}")
    int customAccumulate(@Param("userId") long userId,
                         @Param("statDate") String statDate,
                         @Param("amount") long amount,
                         @Param("dailyLimit") long dailyLimit,
                         @Param("now") long now);
}
