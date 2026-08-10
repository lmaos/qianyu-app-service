package com.clmcat.qianyu.live.room.mapper;

import com.clmcat.qianyu.live.room.model.entity.LiveRoom;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * live_room 表 Mapper。
 * <p>
 * 注意：方法名使用 {@code custom*} 前缀，避免与 MyBatis-Flex BaseMapper 方法冲突。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Mapper
public interface LiveRoomMapper extends BaseMapper<LiveRoom> {

    /**
     * 按对外 room_no 查询直播间。
     */
    @Select("SELECT * FROM live_room WHERE room_no = #{roomNo}")
    LiveRoom customSelectByRoomNo(@Param("roomNo") long roomNo);

    /**
     * 按内部 id 查询直播间。
     */
    @Select("SELECT * FROM live_room WHERE id = #{id}")
    LiveRoom customSelectById(@Param("id") long id);

    /**
     * 按 anchorUserId 查询最近创建的直播间。
     */
    @Select("SELECT * FROM live_room WHERE anchor_user_id = #{anchorUserId} ORDER BY create_time DESC LIMIT 1")
    LiveRoom customSelectLatestByAnchorUserId(@Param("anchorUserId") long anchorUserId);

    /**
     * 查询"直播中"的直播间列表（游标分页，按 room_no 倒序）。
     */
    @Select("<script>" +
            "SELECT * FROM live_room WHERE status = 1" +
            "<if test='nextNo != null and nextNo > 0'> AND room_no &lt; #{nextNo}</if>" +
            " ORDER BY room_no DESC LIMIT #{limit}" +
            "</script>")
    List<LiveRoom> customSelectLiveList(@Param("nextNo") long nextNo, @Param("limit") int limit);

    /**
     * 插入直播间。命名为 customInsert 避免与 BaseMapper.insert 冲突。
     */
    @Insert("INSERT INTO live_room (id, room_no, anchor_user_id, title, cover_image, status, start_time, end_time, create_time, update_time) " +
            "VALUES (#{id}, #{roomNo}, #{anchorUserId}, #{title}, #{coverImage}, #{status}, #{startTime}, #{endTime}, #{createTime}, #{updateTime})")
    int customInsert(LiveRoom liveRoom);

    /**
     * 按内部 id 更新直播间主信息。
     */
    @Update("UPDATE live_room SET title = #{title}, cover_image = #{coverImage}, status = #{status}, start_time = #{startTime}, end_time = #{endTime}, update_time = #{updateTime} WHERE id = #{id}")
    int customUpdateById(LiveRoom liveRoom);

    /**
     * 按内部 id 更新对外直播间编号。
     * 仅 status = 0（待开播）允许修改。
     */
    @Update("UPDATE live_room SET room_no = #{newRoomNo}, update_time = #{updateTime} WHERE id = #{id} AND status = 0")
    int customUpdateRoomNo(@Param("id") long id, @Param("newRoomNo") long newRoomNo, @Param("updateTime") long updateTime);
}
