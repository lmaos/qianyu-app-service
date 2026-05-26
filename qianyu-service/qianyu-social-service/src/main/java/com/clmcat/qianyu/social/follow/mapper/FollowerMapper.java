package com.clmcat.qianyu.social.follow.mapper;

import com.clmcat.qianyu.social.follow.model.entity.Follower;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FollowerMapper extends BaseMapper<Follower> {
    /**
     * 更新粉丝记录的好友状态。
     *
     * @param followeeId 被关注者ID
     * @param followerId 粉丝ID
     * @param isFriend 是否互关好友：0否，1是
     * @return 影响行数
     */
    @Update("UPDATE follower SET is_friend = #{isFriend} WHERE followee_id = #{followeeId} AND follower_id = #{followerId}")
    int updateFriendState(@Param("followeeId") long followeeId, @Param("followerId") long followerId, @Param("isFriend") int isFriend);
}
