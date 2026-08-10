package com.clmcat.qianyu.gift.inventory.mapper;

import com.clmcat.qianyu.gift.inventory.model.entity.UserGiftInventory;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * user_gift_inventory 表 Mapper。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Mapper
public interface UserGiftInventoryMapper extends BaseMapper<UserGiftInventory> {

    /**
     * 按用户ID 游标分页查询背包礼物（按 expire_time 升序，永久的排最后）。
     */
    @Select("<script>" +
            "SELECT * FROM user_gift_inventory WHERE user_id = #{userId} AND status = 1 " +
            "AND (expire_time = 0 OR expire_time &gt; #{now})" +
            "<if test='cursor != null and cursor > 0'> AND id &lt; #{cursor}</if>" +
            " ORDER BY CASE WHEN expire_time = 0 THEN 1 ELSE 0 END, expire_time ASC, id DESC LIMIT #{limit}" +
            "</script>")
    List<UserGiftInventory> customSelectByUserId(@Param("userId") long userId,
                                                  @Param("now") long now,
                                                  @Param("cursor") long cursor,
                                                  @Param("limit") int limit);

    /**
     * 按用户+礼物+过期日期查询单条记录。
     */
    @Select("SELECT * FROM user_gift_inventory WHERE user_id = #{userId} AND gift_id = #{giftId} AND expire_time = #{expireTime}")
    UserGiftInventory customSelectByKey(@Param("userId") long userId,
                                         @Param("giftId") long giftId,
                                         @Param("expireTime") long expireTime);

    /**
     * 发放礼物到背包（INSERT ON DUPLICATE KEY UPDATE 累加）。
     */
    @Insert("INSERT INTO user_gift_inventory (id, user_id, gift_id, quantity, source_type, source_id, expire_time, status, create_time, update_time) " +
            "VALUES (#{id}, #{userId}, #{giftId}, #{quantity}, #{sourceType}, #{sourceId}, #{expireTime}, #{status}, #{createTime}, #{updateTime}) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}, update_time = #{updateTime}")
    int customUpsert(UserGiftInventory record);

    /**
     * 原子扣减背包礼物数量。
     */
    @Update("UPDATE user_gift_inventory SET quantity = quantity - #{quantity}, update_time = #{now} " +
            "WHERE user_id = #{userId} AND gift_id = #{giftId} AND expire_time = #{expireTime} " +
            "AND quantity >= #{quantity} AND (expire_time = 0 OR expire_time > #{now})")
    int customDecrement(@Param("userId") long userId,
                        @Param("giftId") long giftId,
                        @Param("expireTime") long expireTime,
                        @Param("quantity") int quantity,
                        @Param("now") long now);

    /**
     * 标记已过期的库存。
     */
    @Update("UPDATE user_gift_inventory SET status = 2, update_time = #{now} " +
            "WHERE status = 1 AND expire_time > 0 AND expire_time < #{now} LIMIT #{limit}")
    int customMarkExpired(@Param("now") long now, @Param("limit") int limit);

    /**
     * 标记已用完的库存（quantity = 0）。
     */
    @Update("UPDATE user_gift_inventory SET status = 3, update_time = #{now} WHERE quantity <= 0 AND status = 1")
    int customMarkUsedUp(@Param("now") long now);
}
