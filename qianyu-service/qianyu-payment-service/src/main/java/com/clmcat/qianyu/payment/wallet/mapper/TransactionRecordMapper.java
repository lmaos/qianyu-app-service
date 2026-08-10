package com.clmcat.qianyu.payment.wallet.mapper;

import com.clmcat.qianyu.payment.wallet.model.entity.TransactionRecord;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * transaction_record 月表 Mapper。
 * <p>
 * 表名通过 {@code ${tableName}} 动态传入，按 UTC 月度分表。
 * 模板表 transaction_record 仅做 DDL 骨架。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Mapper
public interface TransactionRecordMapper extends BaseMapper<TransactionRecord> {

    @Select("SELECT * FROM ${tableName} WHERE idempotent_key = #{idempotentKey}")
    TransactionRecord customSelectByIdempotentKey(@Param("tableName") String tableName,
                                                   @Param("idempotentKey") String idempotentKey);

    @Select("SELECT * FROM ${tableName} WHERE id = #{id}")
    TransactionRecord customSelectById(@Param("tableName") String tableName,
                                        @Param("id") long id);

    @Select("<script>" +
            "SELECT * FROM ${tableName} WHERE user_id = #{userId}" +
            "<if test='cursor != null and cursor > 0'> AND create_time &lt; #{cursor}</if>" +
            " ORDER BY create_time DESC LIMIT #{limit}" +
            "</script>")
    List<TransactionRecord> customSelectByUserId(@Param("tableName") String tableName,
                                                  @Param("userId") long userId,
                                                  @Param("cursor") long cursor,
                                                  @Param("limit") int limit);

    @Insert("INSERT INTO ${tableName} (id, trans_no, user_id, trans_type, amount, balance_before, balance_after, " +
            "biz_type, biz_id, idempotent_key, status, remark, create_time) " +
            "VALUES (#{record.id}, #{record.transNo}, #{record.userId}, #{record.transType}, #{record.amount}, " +
            "#{record.balanceBefore}, #{record.balanceAfter}, #{record.bizType}, #{record.bizId}, " +
            "#{record.idempotentKey}, #{record.status}, #{record.remark}, #{record.createTime})")
    int customInsert(@Param("tableName") String tableName,
                     @Param("record") TransactionRecord record);

    @Update("UPDATE ${tableName} SET balance_before = #{balanceBefore}, balance_after = #{balanceAfter} WHERE id = #{id}")
    int customUpdateBalances(@Param("tableName") String tableName,
                             @Param("id") long id,
                             @Param("balanceBefore") long balanceBefore,
                             @Param("balanceAfter") long balanceAfter);

    @Update("UPDATE ${tableName} SET status = 2 WHERE id = #{id} AND status = 1")
    int customMarkReversed(@Param("tableName") String tableName,
                           @Param("id") long id);
}
