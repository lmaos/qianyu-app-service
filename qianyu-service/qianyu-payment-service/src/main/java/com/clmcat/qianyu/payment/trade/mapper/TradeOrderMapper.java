package com.clmcat.qianyu.payment.trade.mapper;

import com.clmcat.qianyu.payment.trade.model.entity.TradeOrder;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * trade_order 表 Mapper。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrder> {

    @Select("SELECT * FROM trade_order WHERE id = #{id}")
    TradeOrder customSelectById(@Param("id") long id);

    @Select("SELECT * FROM trade_order WHERE trans_no = #{transNo}")
    TradeOrder customSelectByTransNo(@Param("transNo") String transNo);

    @Select("SELECT * FROM trade_order WHERE idempotent_key = #{idempotentKey}")
    TradeOrder customSelectByIdempotentKey(@Param("idempotentKey") String idempotentKey);

    @Insert("INSERT INTO trade_order (id, trans_no, from_user_id, coin_amount, " +
            "biz_type, biz_id, idempotent_key, status, create_time) " +
            "VALUES (#{id}, #{transNo}, #{fromUserId}, #{coinAmount}, " +
            "#{bizType}, #{bizId}, #{idempotentKey}, #{status}, #{createTime})")
    int customInsert(TradeOrder order);

    /**
     * 更新订单状态。使用乐观锁约束：仅当前状态匹配时才更新，防止并发。
     * 返回受影响行数，0 表示状态已变更或订单不存在。
     */
    @Update("UPDATE trade_order SET status = #{newStatus} WHERE id = #{id} AND status = #{expectedStatus}")
    int customUpdateStatus(@Param("id") long id,
                           @Param("expectedStatus") int expectedStatus,
                           @Param("newStatus") int newStatus);

    /**
     * 查询超时未确认的 PENDING 订单（用于定时清理）。
     */
    @Select("SELECT * FROM trade_order WHERE status = 0 AND create_time < #{deadline} LIMIT #{limit}")
    java.util.List<TradeOrder> customSelectPendingTimeout(@Param("deadline") long deadline,
                                                           @Param("limit") int limit);
}
