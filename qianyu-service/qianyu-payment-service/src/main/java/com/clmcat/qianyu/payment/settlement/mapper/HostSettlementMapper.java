package com.clmcat.qianyu.payment.settlement.mapper;

import com.clmcat.qianyu.payment.settlement.model.entity.HostSettlement;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * host_settlement 表 Mapper。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Mapper
public interface HostSettlementMapper extends BaseMapper<HostSettlement> {

    @Select("SELECT * FROM host_settlement WHERE user_id = #{userId}")
    HostSettlement customSelectByUserId(@Param("userId") long userId);

    @Insert("INSERT INTO host_settlement (user_id, balance, total_earning, frozen_balance, status, version, create_time, update_time) " +
            "VALUES (#{userId}, 0, 0, 0, 1, 0, #{createTime}, #{updateTime})")
    int customInsert(@Param("userId") long userId,
                     @Param("createTime") long createTime,
                     @Param("updateTime") long updateTime);

    /** 原子收入 */
    @Update("UPDATE host_settlement SET balance = balance + #{amount}, " +
            "total_earning = total_earning + #{amount}, " +
            "version = version + 1, " +
            "update_time = #{updateTime} " +
            "WHERE user_id = #{userId}")
    int customCredit(@Param("userId") long userId,
                     @Param("amount") long amount,
                     @Param("updateTime") long updateTime);
}
