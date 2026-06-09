package com.clmcat.qianyu.social.api.follow;

import com.clmcat.qianyu.social.api.follow.model.dto.FollowDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowCountDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowListDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowRelationDto;

import java.util.List;

public interface FollowApi {
    /**
     * 关注这个人
     *
     * @param dto 关注关系参数，followerId 表示当前用户，followeeId 表示目标用户
     */
    void follow(FollowDto dto);

    /**
     * 直接取消关注
     *
     * @param dto 取消关注参数，followerId 表示当前用户，followeeId 表示目标用户
     */
    void cancelFollow(FollowDto dto);

    /**
     * 是否存在关注关系
     *
     * @param dto 查询参数，followerId 关注者，followeeId 被关注者
     * @return true 表示已关注
     */
    boolean existsFollow(FollowDto dto);

    /**
     * 查询两个用户之间的关注关系
     *
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 关注关系 DTO
     */
    FollowRelationDto getRelation(long userId, long targetUserId);

    /**
     * 查询用户的关注列表（我关注了谁）
     *
     * @param followerId 当前查询哪个用户的关注列表
     * @param nextId 游标ID，只查询比它更早的关系记录
     * @param limit 查询条数
     * @return 关注关系列表
     */
    FollowListDto getFollowListByFollowerId(long followerId, long nextId, int limit);

    /**
     * 查询用户的粉丝列表（谁关注了我）
     *
     * @param followeeId 当前查询哪个用户的粉丝列表
     * @param nextId 游标ID，只查询比它更早的关系记录
     * @param limit 查询条数
     * @return 粉丝关系列表
     */
    FollowListDto getFollowerListByFolloweeId(long followeeId, long nextId, int limit);

    /**
     * 查询用户关注/粉丝数量
     *
     * @param userId 用户ID
     * @return 关注数/粉丝数 DTO
     */
    FollowCountDto getFollowCount(long userId);

    /**
     * 查询用户的联系人（好友）ID 列表。
     *
     * @param userId 用户ID；联系人从 follower 表按 followee_id=userId 且 is_friend=1 读取
     * @return 好友用户ID列表
     */
    List<Long> getFriendIdsByUserId(long userId);

}
