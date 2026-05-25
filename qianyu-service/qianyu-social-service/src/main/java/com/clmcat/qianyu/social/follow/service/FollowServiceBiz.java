package com.clmcat.qianyu.social.follow.service;

import com.clmcat.qianyu.social.api.base.model.dto.UserSocialCounterDto;
import com.clmcat.qianyu.social.api.follow.FollowApi;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowCountDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowRelationDto;
import com.clmcat.qianyu.social.base.service.UserSocialCounterServiceBiz;
import com.clmcat.qianyu.social.follow.mapper.FollowMapper;
import com.clmcat.qianyu.social.follow.mapper.FollowerMapper;
import com.clmcat.qianyu.social.follow.model.entity.Follow;
import com.clmcat.qianyu.social.follow.model.entity.Follower;
import com.clmcat.qianyu.social.follow.model.entity.status.Status;
import com.clmcat.qianyu.social.follow.support.FollowSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@DubboService
@Slf4j
public class FollowServiceBiz implements FollowApi {

    @Resource
    FollowMapper followMapper ;

    @Resource
    FollowerMapper followerMapper;

    @Resource
    UserSocialCounterServiceBiz userSocialCounterServiceBiz;

    /**
     * 写入关注关系。
     *
     * @param dto 关注参数，followerId 表示关注者，followeeId 表示被关注者
     */
    @Override
    public void follow(FollowDto dto) {
        verify(dto);
        long id = FollowSupport.FOLLOW_ID_SNOWFLAKE.nextId();
        long time = com.clmcat.qianyu.core.snowflake.SnowflakeSupport.parseTimeBySnowflake(FollowSupport.FOLLOW_ID_SNOWFLAKE, id);
        boolean reverseFollow = existsFollow(dto.getFolloweeId(), dto.getFollowerId());
        // 关注
        Follow follow = new Follow();
        follow.setId(id);
        follow.setFollowerId(dto.getFollowerId());
        follow.setFolloweeId(dto.getFolloweeId());
        follow.setClientTime(time);

        // 粉丝
        Follower follower= new Follower();
        follower.setId(id);
        follower.setFollowerId(dto.getFollowerId());
        follower.setFolloweeId(dto.getFolloweeId());
        follower.setClientTime(time);

        try {
            // TODO 增加事务写入， 暂时未增加。
            followMapper.insert(follow);
            followerMapper.insert(follower);
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(dto.getFollowerId())
                    .followCount(1L)
                    .build());
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(dto.getFolloweeId())
                    .followerCount(1L)
                    .build());
            if (reverseFollow) {
                userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                        .userId(dto.getFollowerId())
                        .friendCount(1L)
                        .build());
                userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                        .userId(dto.getFolloweeId())
                        .friendCount(1L)
                        .build());
            }
        }  catch (DuplicateKeyException e) {
            log.debug("follow already exists, followerId={}, followeeId={}", dto.getFollowerId(), dto.getFolloweeId());
        }
    }

    /**
     * 取消关注这个人
     *
     * @param dto 取消关注参数，followerId 表示关注者，followeeId 表示被关注者
     */
    @Override
    public void cancelFollow(FollowDto dto) {
        verify(dto);
        QueryWrapper followQueryWrapper = QueryWrapper.create();
        followQueryWrapper.eq(Follow::getFollowerId, dto.getFollowerId());
        followQueryWrapper.eq(Follow::getFolloweeId, dto.getFolloweeId());
        followQueryWrapper.select("id");
        Long id = followMapper.selectObjectByQueryAs(followQueryWrapper, Long.class);
        if (id == null) {
            // 可能已经取消了。所以没查到ID
            return;
        }
        boolean reverseFollow = existsFollow(dto.getFolloweeId(), dto.getFollowerId());

        // TODO 通过事务删除，暂未增加。
        followMapper.deleteById(id);
        followerMapper.deleteById(id);
        userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                .userId(dto.getFollowerId())
                .followCount(-1L)
                .build());
        userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                .userId(dto.getFolloweeId())
                .followerCount(-1L)
                .build());
        if (reverseFollow) {
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(dto.getFollowerId())
                    .friendCount(-1L)
                    .build());
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(dto.getFolloweeId())
                    .friendCount(-1L)
                    .build());
        }

    }

    /**
     * 判断是否已存在关注关系。
     *
     * @param dto 查询参数，followerId 为关注者，followeeId 为被关注者
     * @return true 表示已关注
     */
    @Override
    public boolean existsFollow(FollowDto dto) {
        if (dto == null) {
            return false;
        }
        return existsFollow(dto.getFollowerId(), dto.getFolloweeId());
    }

    /**
     * 查询两个用户之间的关注关系。
     *
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 关注关系 DTO，包含是否关注、是否被关注、是否互关
     */
    @Override
    public FollowRelationDto getRelation(long userId, long targetUserId) {
        if (userId <= 0 || targetUserId <= 0) {
            return FollowRelationDto.builder()
                    .userId(userId)
                    .targetUserId(targetUserId)
                    .build();
        }
        boolean follow = existsFollow(userId, targetUserId);
        boolean follower = existsFollow(targetUserId, userId);
        return FollowRelationDto.builder()
                .userId(userId)
                .targetUserId(targetUserId)
                .follow(follow)
                .follower(follower)
                .friend(follow && follower)
                .build();
    }

    /**
     * 查询关注列表。
     *
     * @param followerId 查询哪个用户关注了谁
     * @param nextId 游标ID，倒序翻页时传上一页最后一条关系ID
     * @param limit 本次查询条数
     * @return 关注关系 DTO 列表
     */
    @Override
    public List<FollowDto> getFollowListByFollowerId(long followerId, long nextId, int limit) {
        if (followerId <= 0 || limit <= 0) {
            return new ArrayList<>();
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(Follow::getFollowerId, followerId);
        queryWrapper.lt(Follow::getId, nextId);
        queryWrapper.orderBy(Follow::getId, false);
        queryWrapper.limit(limit);
        List<Follow> follows = followMapper.selectListByQuery(queryWrapper);
        return FollowSupport.toFollowDtoListFromFollow(follows);
    }

    /**
     * 查询粉丝列表。
     *
     * @param followeeId 查询哪个用户的粉丝
     * @param nextId 游标ID，倒序翻页时传上一页最后一条关系ID
     * @param limit 本次查询条数
     * @return 粉丝关系 DTO 列表
     */
    @Override
    public List<FollowDto> getFollowerListByFolloweeId(long followeeId, long nextId, int limit) {
        if (followeeId <= 0 || limit <= 0) {
            return new ArrayList<>();
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(Follower::getFolloweeId, followeeId);
        queryWrapper.lt(Follower::getId, nextId);
        queryWrapper.orderBy(Follower::getId, false);
        queryWrapper.limit(limit);
        List<Follower> followers = followerMapper.selectListByQuery(queryWrapper);
        return FollowSupport.toFollowDtoListFromFollower(followers);
    }

    /**
     * 查询用户的关注数和粉丝数。
     *
     * @param userId 用户ID
     * @return 数量 DTO
     */
    @Override
    public FollowCountDto getFollowCount(long userId) {
        if (userId <= 0) {
            return FollowCountDto.builder()
                    .userId(userId)
                    .followCount(0L)
                    .followerCount(0L)
                    .build();
        }

        long followCount = countFollowByFollowerId(userId);
        long followerCount = countFollowerByFolloweeId(userId);
        return FollowCountDto.builder()
                .userId(userId)
                .followCount(followCount)
                .followerCount(followerCount)
                .build();
    }

    private boolean existsFollow(long followerId, long followeeId) {
        if (followerId <= 0 || followeeId <= 0) {
            return false;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFolloweeId, followeeId)
                .select("id");
        return followMapper.selectObjectByQuery(queryWrapper) != null;
    }

    private long countFollowByFollowerId(long followerId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Follow::getFollowerId, followerId)
                .select("count(*)");
        Long count = followMapper.selectObjectByQueryAs(queryWrapper, Long.class);
        return count == null ? 0L : count;
    }

    private long countFollowerByFolloweeId(long followeeId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Follower::getFolloweeId, followeeId)
                .select("count(*)");
        Long count = followerMapper.selectObjectByQueryAs(queryWrapper, Long.class);
        return count == null ? 0L : count;
    }

    private void verify(FollowDto dto) {
        Status.FOLLOWER_REQUIRED.assertThrowResEx(dto == null || FollowSupport.isNullOrNonPositive(dto.getFollowerId()));
        Status.FOLLOWEE_REQUIRED.assertThrowResEx(dto == null || FollowSupport.isNullOrNonPositive(dto.getFolloweeId()));
        Status.FOLLOW_SELF_NOT_ALLOWED.assertThrowResEx(dto.getFollowerId().equals(dto.getFolloweeId()));
    }
}
