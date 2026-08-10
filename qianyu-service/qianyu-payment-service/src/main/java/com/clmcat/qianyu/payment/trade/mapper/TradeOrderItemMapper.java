package com.clmcat.qianyu.payment.trade.mapper;

import com.clmcat.qianyu.payment.trade.model.entity.TradeOrderItem;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * trade_order_item 月表 Mapper。
 * <p>
 * 表名通过 {@code ${tableName}} 动态传入，按 UTC 月度分表写入月表 trade_order_item_{yyyyMM}。
 * 模板表 trade_order_item 仅做 DDL 模板，不存数据。
 *
 * @author ark-home
 * @date 2026-08-10
 */
@Mapper
public interface TradeOrderItemMapper extends BaseMapper<TradeOrderItem> {

    /**
     * 批量插入结算子项（一条 SQL，<foreach> 拼接 VALUES）。
     */
    @org.apache.ibatis.annotations.Insert("<script>" +
            "INSERT INTO ${tableName} (id, order_id, trans_no, biz_no, from_user_id, to_user_id, settle_amount, commission_rate, status, create_time) VALUES " +
            "<foreach collection='items' item='item' separator=','>" +
            "(#{item.id}, #{orderId}, #{item.transNo}, #{item.bizNo}, #{item.fromUserId}, #{item.toUserId}, #{item.settleAmount}, #{item.commissionRate}, #{item.status}, #{item.createTime})" +
            "</foreach>" +
            "</script>")
    int customBatchInsert(@Param("tableName") String tableName,
                          @Param("orderId") long orderId,
                          @Param("items") List<TradeOrderItem> items);

    /**
     * 按订单 ID 查询所有子项（指定月表）。
     */
    @Select("SELECT * FROM ${tableName} WHERE order_id = #{orderId}")
    List<TradeOrderItem> customSelectByOrderId(@Param("tableName") String tableName,
                                                @Param("orderId") long orderId);

    /**
     * 更新子项状态（指定月表）。乐观锁：仅当前状态匹配时才更新。
     */
    @Update("UPDATE ${tableName} SET status = #{newStatus} WHERE id = #{id} AND status = #{expectedStatus}")
    int customUpdateStatus(@Param("tableName") String tableName,
                           @Param("id") long id,
                           @Param("expectedStatus") int expectedStatus,
                           @Param("newStatus") int newStatus);

    // ==================== 预留查询方法 ====================

    /**
     * 按消费流水号查询（指定月表）。一次扣款对应 1~N 条结算子项。
     */
    @Select("SELECT * FROM ${tableName} WHERE trans_no = #{transNo} ORDER BY id")
    List<TradeOrderItem> customSelectByTransNo(@Param("tableName") String tableName,
                                                @Param("transNo") String transNo);

    /**
     * 按付款方游标分页（"我送过的"列表，指定月表）。
     * <p>
     * 使用模式同 gift_send_record：从当月往前逐月查，凑够 limit。
     */
    @Select("<script>" +
            "SELECT * FROM ${tableName} WHERE from_user_id = #{userId}" +
            "<if test='cursor != null and cursor > 0'> AND id &lt; #{cursor}</if>" +
            " ORDER BY id DESC LIMIT #{limit}" +
            "</script>")
    List<TradeOrderItem> customSelectByFromUserId(@Param("tableName") String tableName,
                                                   @Param("userId") long userId,
                                                   @Param("cursor") long cursor,
                                                   @Param("limit") int limit);

    /**
     * 按收款方（主播）游标分页（"收到的结算"列表，指定月表）。
     */
    @Select("<script>" +
            "SELECT * FROM ${tableName} WHERE to_user_id = #{userId}" +
            "<if test='cursor != null and cursor > 0'> AND id &lt; #{cursor}</if>" +
            " ORDER BY id DESC LIMIT #{limit}" +
            "</script>")
    List<TradeOrderItem> customSelectByToUserId(@Param("tableName") String tableName,
                                                 @Param("userId") long userId,
                                                 @Param("cursor") long cursor,
                                                 @Param("limit") int limit);
}
