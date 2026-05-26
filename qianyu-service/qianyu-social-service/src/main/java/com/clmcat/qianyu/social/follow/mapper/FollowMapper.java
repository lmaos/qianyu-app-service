package com.clmcat.qianyu.social.follow.mapper;

import com.clmcat.qianyu.social.follow.model.entity.Follow;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
    /**
     * 更新单向关注记录的好友状态。
     *
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @param isFriend 是否互关好友：0否，1是
     * @return 影响行数
     */
    @Update("UPDATE follow SET is_friend = #{isFriend} WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    int updateFriendState(@Param("followerId") long followerId, @Param("followeeId") long followeeId, @Param("isFriend") int isFriend);
}
