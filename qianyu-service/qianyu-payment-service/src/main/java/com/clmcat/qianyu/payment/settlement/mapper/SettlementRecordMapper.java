package com.clmcat.qianyu.payment.settlement.mapper;

import com.clmcat.qianyu.payment.settlement.model.entity.SettlementRecord;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * settlement_record 月表 Mapper。
 * <p>
 * 表名通过 {@code ${tableName}} 动态传入，按 UTC 月度分表。
 * 模板表 settlement_record 仅做 DDL 骨架。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Mapper
public interface SettlementRecordMapper extends BaseMapper<SettlementRecord> {

    @Select("SELECT * FROM ${tableName} WHERE idempotent_key = #{idempotentKey}")
    SettlementRecord customSelectByIdempotentKey(@Param("tableName") String tableName,
                                                  @Param("idempotentKey") String idempotentKey);

    @Select("SELECT * FROM ${tableName} WHERE id = #{id}")
    SettlementRecord customSelectById(@Param("tableName") String tableName,
                                       @Param("id") long id);

    @Select("<script>" +
            "SELECT * FROM ${tableName} WHERE user_id = #{userId}" +
            "<if test='cursor != null and cursor > 0'> AND id &lt; #{cursor}</if>" +
            " ORDER BY id DESC LIMIT #{limit}" +
            "</script>")
    List<SettlementRecord> customSelectByUserId(@Param("tableName") String tableName,
                                                 @Param("userId") long userId,
                                                 @Param("cursor") long cursor,
                                                 @Param("limit") int limit);

    @Insert("INSERT INTO ${tableName} (id, trans_no, biz_no, user_id, settle_type, amount, balance_before, balance_after, " +
            "commission_rate, idempotent_key, status, remark, create_time) " +
            "VALUES (#{record.id}, #{record.transNo}, #{record.bizNo}, #{record.userId}, #{record.settleType}, #{record.amount}, " +
            "#{record.balanceBefore}, #{record.balanceAfter}, #{record.commissionRate}, #{record.idempotentKey}, " +
            "#{record.status}, #{record.remark}, #{record.createTime})")
    int customInsert(@Param("tableName") String tableName,
                     @Param("record") SettlementRecord record);

    @Update("UPDATE ${tableName} SET balance_before = #{balanceBefore}, balance_after = #{balanceAfter} WHERE id = #{id}")
    int customUpdateBalances(@Param("tableName") String tableName,
                             @Param("id") long id,
                             @Param("balanceBefore") long balanceBefore,
                             @Param("balanceAfter") long balanceAfter);
}
