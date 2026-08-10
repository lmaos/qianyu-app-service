package com.clmcat.qianyu.live.room.mapper;

import com.clmcat.qianyu.live.room.model.entity.LiveRoomCount;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * live_room_count 表 Mapper。
 *
 * @author ark-home
 * @date 2026-07-08
 */
@Mapper
public interface LiveRoomCountMapper extends BaseMapper<LiveRoomCount> {

    /**
     * 按 roomId（内部逻辑主键）查询计数器。
     */
    @Select("SELECT * FROM live_room_count WHERE room_id = #{roomId}")
    LiveRoomCount customSelectByRoomId(@Param("roomId") long roomId);

    /**
     * 创建计数器记录（创建直播间时调用）。
     */
    @Insert("INSERT INTO live_room_count (room_id, viewer_count, max_online_count, like_count, gift_count, gift_amount, comment_count, share_count) " +
            "VALUES (#{roomId}, 0, 0, 0, 0, 0, 0, 0)")
    int customInsert(@Param("roomId") long roomId);

    /**
     * 开播时重置计数器为 0。
     */
    @Update("UPDATE live_room_count SET viewer_count = 0, max_online_count = 0, like_count = 0, " +
            "gift_count = 0, gift_amount = 0, comment_count = 0, share_count = 0 WHERE room_id = #{roomId}")
    int customResetCount(@Param("roomId") long roomId);

    /**
     * 观看人数 +1。
     */
    @Update("UPDATE live_room_count SET viewer_count = viewer_count + 1 WHERE room_id = #{roomId}")
    int customIncrViewerCount(@Param("roomId") long roomId);

    /**
     * 点赞数 +1。
     */
    @Update("UPDATE live_room_count SET like_count = like_count + 1 WHERE room_id = #{roomId}")
    int customIncrLikeCount(@Param("roomId") long roomId);

    /**
     * 礼物数 +count，礼物金额 +amount。
     */
    @Update("UPDATE live_room_count SET gift_count = gift_count + #{count}, gift_amount = gift_amount + #{amount} WHERE room_id = #{roomId}")
    int customIncrGiftCount(@Param("roomId") long roomId, @Param("count") long count, @Param("amount") long amount);
}
