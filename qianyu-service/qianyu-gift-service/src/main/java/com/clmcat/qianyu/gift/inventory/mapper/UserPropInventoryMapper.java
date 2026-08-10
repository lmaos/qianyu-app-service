package com.clmcat.qianyu.gift.inventory.mapper;

import com.clmcat.qianyu.gift.inventory.model.entity.UserPropInventory;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * user_prop_inventory 表 Mapper。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Mapper
public interface UserPropInventoryMapper extends BaseMapper<UserPropInventory> {

    /**
     * 按 ID 查询。
     */
    @Select("SELECT * FROM user_prop_inventory WHERE id = #{id}")
    UserPropInventory customSelectById(@Param("id") long id);

    /**
     * 按用户ID 查询所有有效道具（按获取时间倒序）。
     */
    @Select("SELECT * FROM user_prop_inventory WHERE user_id = #{userId} AND status IN (0, 1) " +
            "AND (expire_time = 0 OR expire_time > #{now}) " +
            "ORDER BY obtain_time DESC")
    List<UserPropInventory> customSelectByUserId(@Param("userId") long userId, @Param("now") long now);

    /**
     * 按用户+道具类型查询当前穿戴的道具。
     */
    @Select("SELECT * FROM user_prop_inventory WHERE user_id = #{userId} AND prop_type = #{propType} AND status = 1 " +
            "AND (expire_time = 0 OR expire_time > #{now})")
    UserPropInventory customSelectEquippedByType(@Param("userId") long userId,
                                                  @Param("propType") String propType,
                                                  @Param("now") long now);

    /**
     * 按用户ID 查询所有当前穿戴的道具。
     */
    @Select("SELECT * FROM user_prop_inventory WHERE user_id = #{userId} AND status = 1 " +
            "AND (expire_time = 0 OR expire_time > #{now})")
    List<UserPropInventory> customSelectAllEquipped(@Param("userId") long userId, @Param("now") long now);

    /**
     * 插入道具记录。
     */
    @Insert("INSERT INTO user_prop_inventory (id, user_id, prop_id, prop_type, status, source_type, source_id, " +
            "obtain_time, expire_time, create_time, update_time) " +
            "VALUES (#{id}, #{userId}, #{propId}, #{propType}, #{status}, #{sourceType}, #{sourceId}, " +
            "#{obtainTime}, #{expireTime}, #{createTime}, #{updateTime})")
    int customInsert(UserPropInventory record);

    /**
     * 更新道具状态（穿戴/卸下/使用）。
     */
    @Update("UPDATE user_prop_inventory SET status = #{status}, update_time = #{now} " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int customUpdateStatus(@Param("id") long id,
                           @Param("userId") long userId,
                           @Param("status") int status,
                           @Param("now") long now);

    /**
     * 将同类型已穿戴的道具全部卸下。
     */
    @Update("UPDATE user_prop_inventory SET status = 0, update_time = #{now} " +
            "WHERE user_id = #{userId} AND prop_type = #{propType} AND status = 1")
    int customUnequipByType(@Param("userId") long userId,
                            @Param("propType") String propType,
                            @Param("now") long now);

    /**
     * 标记已过期的道具。
     */
    @Update("UPDATE user_prop_inventory SET status = 3, update_time = #{now} " +
            "WHERE status IN (0, 1) AND expire_time > 0 AND expire_time < #{now} LIMIT #{limit}")
    int customMarkExpired(@Param("now") long now, @Param("limit") int limit);
}
