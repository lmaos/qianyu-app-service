package com.clmcat.qianyu.payment.wallet.mapper;

import com.clmcat.qianyu.payment.wallet.model.entity.UserWallet;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * user_wallet 表 Mapper。
 * <p>
 * 注意：方法名使用 {@code custom*} 前缀，避免与 MyBatis-Flex BaseMapper 方法冲突。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {

    /**
     * 按 userId 查询钱包。
     */
    @Select("SELECT * FROM user_wallet WHERE user_id = #{userId}")
    UserWallet customSelectByUserId(@Param("userId") long userId);

    /**
     * 创建钱包（新用户首次操作时调用，初始余额为 0）。
     */
    @Insert("INSERT INTO user_wallet (user_id, balance, frozen_balance, total_income, total_expense, version, create_time, update_time) " +
            "VALUES (#{userId}, 0, 0, 0, 0, 0, #{createTime}, #{updateTime})")
    int customInsert(@Param("userId") long userId,
                     @Param("createTime") long createTime,
                     @Param("updateTime") long updateTime);

    /**
     * 原子收入：balance = balance + amount，total_income = total_income + amount。
     * 返回受影响行数，0 表示用户钱包不存在。
     */
    @Update("UPDATE user_wallet SET balance = balance + #{amount}, " +
            "total_income = total_income + #{amount}, " +
            "version = version + 1, " +
            "update_time = #{updateTime} " +
            "WHERE user_id = #{userId}")
    int customCredit(@Param("userId") long userId,
                     @Param("amount") long amount,
                     @Param("updateTime") long updateTime);

    /**
     * 原子支出：balance = balance - amount，total_expense = total_expense + amount。
     * WHERE balance >= amount 防止透支。返回受影响行数，0 表示余额不足或钱包不存在。
     */
    @Update("UPDATE user_wallet SET balance = balance - #{amount}, " +
            "total_expense = total_expense + #{amount}, " +
            "version = version + 1, " +
            "update_time = #{updateTime} " +
            "WHERE user_id = #{userId} AND balance >= #{amount}")
    int customDeduct(@Param("userId") long userId,
                     @Param("amount") long amount,
                     @Param("updateTime") long updateTime);

    /**
     * 冻结余额：balance → frozen_balance。
     * WHERE balance >= amount 防止透支（并发安全）。
     * 返回受影响行数，0 表示余额不足或钱包不存在。
     */
    @Update("UPDATE user_wallet SET balance = balance - #{amount}, " +
            "frozen_balance = frozen_balance + #{amount}, " +
            "version = version + 1, " +
            "update_time = #{updateTime} " +
            "WHERE user_id = #{userId} AND balance >= #{amount}")
    int customFreeze(@Param("userId") long userId,
                     @Param("amount") long amount,
                     @Param("updateTime") long updateTime);

    /**
     * 确认冻结（扣款完成）：清除已冻结的余额。
     * frozen_balance = frozen_balance - amount，total_expense = total_expense + amount。
     */
    @Update("UPDATE user_wallet SET frozen_balance = frozen_balance - #{amount}, " +
            "total_expense = total_expense + #{amount}, " +
            "version = version + 1, " +
            "update_time = #{updateTime} " +
            "WHERE user_id = #{userId} AND frozen_balance >= #{amount}")
    int customConfirmFreeze(@Param("userId") long userId,
                            @Param("amount") long amount,
                            @Param("updateTime") long updateTime);

    /**
     * 解冻余额（取消订单）：frozen_balance → balance。
     */
    @Update("UPDATE user_wallet SET balance = balance + #{amount}, " +
            "frozen_balance = frozen_balance - #{amount}, " +
            "version = version + 1, " +
            "update_time = #{updateTime} " +
            "WHERE user_id = #{userId} AND frozen_balance >= #{amount}")
    int customUnfreeze(@Param("userId") long userId,
                       @Param("amount") long amount,
                       @Param("updateTime") long updateTime);
}
