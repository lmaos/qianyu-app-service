package com.clmcat.qianyu.gift.gift.mapper;

import com.clmcat.qianyu.gift.gift.model.entity.GiftSendRecord;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * gift_send_record 月表 Mapper。
 * <p>
 * 表名通过 {@code ${tableName}} 动态传入，由 {@link com.clmcat.qianyu.core.table.MonthTableRouter} 计算。
 * 模板表 gift_send_record 仅做 DDL 模板，业务数据全部写入月表 gift_send_record_{yyyyMM}。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Mapper
public interface GiftSendRecordMapper extends BaseMapper<GiftSendRecord> {

    /**
     * 按 ID 查询（指定月表）。
     */
    @Select("SELECT * FROM ${tableName} WHERE id = #{id}")
    GiftSendRecord customSelectById(@Param("tableName") String tableName,
                                    @Param("id") long id);

    /**
     * 按幂等键查询（防重核心，指定月表）。
     */
    @Select("SELECT * FROM ${tableName} WHERE idempotent_key = #{idempotentKey}")
    GiftSendRecord customSelectByIdempotentKey(@Param("tableName") String tableName,
                                                @Param("idempotentKey") String idempotentKey);

    /**
     * 插入送礼记录（指定月表）。
     */
    @Insert("INSERT INTO ${tableName} (id, trans_no, biz_no, sender_user_id, receiver_user_id, gift_id, gift_name, " +
            "gift_price, quantity, total_amount, actual_gift_id, actual_gift_name, scene_type, room_id, " +
            "pay_type, idempotent_key, commission_rate, settle_amount, status, remark, create_time) " +
            "VALUES (#{record.id}, #{record.transNo}, #{record.bizNo}, #{record.senderUserId}, #{record.receiverUserId}, " +
            "#{record.giftId}, #{record.giftName}, #{record.giftPrice}, #{record.quantity}, #{record.totalAmount}, " +
            "#{record.actualGiftId}, #{record.actualGiftName}, #{record.sceneType}, #{record.roomId}, " +
            "#{record.payType}, #{record.idempotentKey}, #{record.commissionRate}, #{record.settleAmount}, " +
            "#{record.status}, #{record.remark}, #{record.createTime})")
    int customInsert(@Param("tableName") String tableName,
                     @Param("record") GiftSendRecord record);

    /**
     * 回填送礼记录的结算信息（盲盒/延迟结算场景）。
     */
    @Update("UPDATE ${tableName} SET actual_gift_id = #{actualGiftId}, actual_gift_name = #{actualGiftName}, " +
            "settle_amount = #{settleAmount}, commission_rate = #{commissionRate} WHERE id = #{id}")
    int customUpdateSettleInfo(@Param("tableName") String tableName,
                               @Param("id") long id,
                               @Param("actualGiftId") Long actualGiftId,
                               @Param("actualGiftName") String actualGiftName,
                               @Param("settleAmount") long settleAmount,
                               @Param("commissionRate") int commissionRate);

    /**
     * 按用户ID 游标分页查询送礼记录（指定月表）。
     */
    @Select("<script>" +
            "SELECT * FROM ${tableName} WHERE sender_user_id = #{userId}" +
            "<if test='cursor != null and cursor > 0'> AND id &lt; #{cursor}</if>" +
            " ORDER BY id DESC LIMIT #{limit}" +
            "</script>")
    List<GiftSendRecord> customSelectByUserId(@Param("tableName") String tableName,
                                               @Param("userId") long userId,
                                               @Param("cursor") long cursor,
                                               @Param("limit") int limit);

    /**
     * 按直播间ID 查询送礼记录（指定月表）。
     */
    @Select("<script>" +
            "SELECT * FROM ${tableName} WHERE room_id = #{roomId}" +
            "<if test='cursor != null and cursor > 0'> AND id &lt; #{cursor}</if>" +
            " ORDER BY id DESC LIMIT #{limit}" +
            "</script>")
    List<GiftSendRecord> customSelectByRoomId(@Param("tableName") String tableName,
                                               @Param("roomId") long roomId,
                                               @Param("cursor") long cursor,
                                               @Param("limit") int limit);

    /**
     * 更新送礼记录状态（指定月表）。乐观锁：仅当前状态匹配时才更新。
     */
    @Update("UPDATE ${tableName} SET status = #{newStatus} WHERE id = #{id} AND status = #{expectedStatus}")
    int customUpdateStatus(@Param("tableName") String tableName,
                           @Param("id") long id,
                           @Param("expectedStatus") int expectedStatus,
                           @Param("newStatus") int newStatus);

    /**
     * 标记送礼记录为已退款（指定月表）。
     */
    @Update("UPDATE ${tableName} SET status = 2 WHERE id = #{id} AND status = 1")
    int customMarkRefunded(@Param("tableName") String tableName,
                           @Param("id") long id);

    // ==================== 预留查询方法 ====================

    /**
     * 按收礼人（主播）游标分页（指定月表）。
     * <p>
     * 使用模式：逐月查，凑够 limit 条为止。
     * <pre>{@code
     *   for (String month : MonthTableRouter.queryMonths(currentMonth(), maxMonths)) {
     *       String tbl = MonthTableRouter.tableName("gift_send_record", month);
     *       List<GiftSendRecord> batch = mapper.customSelectByReceiverUserId(tbl, receiverId, cursor, limit - result.size());
     *       result.addAll(batch);
     *       if (result.size() >= limit) break;
     *   }
     * }</pre>
     */
    @Select("<script>" +
            "SELECT * FROM ${tableName} WHERE receiver_user_id = #{userId}" +
            "<if test='cursor != null and cursor > 0'> AND id &lt; #{cursor}</if>" +
            " ORDER BY id DESC LIMIT #{limit}" +
            "</script>")
    List<GiftSendRecord> customSelectByReceiverUserId(@Param("tableName") String tableName,
                                                       @Param("userId") long userId,
                                                       @Param("cursor") long cursor,
                                                       @Param("limit") int limit);

    /**
     * 按消费流水号查询（指定月表）。一次扣款可能对应多条送礼记录（批量场景）。
     */
    @Select("SELECT * FROM ${tableName} WHERE trans_no = #{transNo} ORDER BY id")
    List<GiftSendRecord> customSelectByTransNo(@Param("tableName") String tableName,
                                                @Param("transNo") String transNo);

    /**
     * 按状态查询 PENDING 记录（对账/补偿用）。
     */
    @Select("SELECT * FROM ${tableName} WHERE status = #{status} AND create_time < #{beforeTime} LIMIT #{limit}")
    List<GiftSendRecord> customSelectByStatusBefore(@Param("tableName") String tableName,
                                                     @Param("status") int status,
                                                     @Param("beforeTime") long beforeTime,
                                                     @Param("limit") int limit);

    /**
     * 统计直播间内送礼总数（指定月表）。
     */
    @Select("SELECT COUNT(*) FROM ${tableName} WHERE room_id = #{roomId} AND status = 1")
    long customCountByRoomId(@Param("tableName") String tableName,
                             @Param("roomId") long roomId);
}
