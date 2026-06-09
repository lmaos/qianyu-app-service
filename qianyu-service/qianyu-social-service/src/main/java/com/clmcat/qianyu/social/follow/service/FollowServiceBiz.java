package com.clmcat.qianyu.social.follow.service;

import com.clmcat.qianyu.social.api.base.model.dto.UserSocialCounterDto;
import com.clmcat.qianyu.social.api.follow.FollowApi;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowCountDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowListDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowRelationDto;
import com.clmcat.qianyu.core.redis.RedisLock;
import com.clmcat.qianyu.core.redis.RedisLockSupport;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@DubboService
@Slf4j
public class FollowServiceBiz implements FollowApi {

    private static final long FOLLOW_LOCK_WAIT_MILLIS = 1200L;
    private static final long FOLLOW_LOCK_MAX_HOLD_SECONDS = 10L;
    private static final String FOLLOW_LOCK_KEY_PREFIX = "social:follow:pair:lock:";

    @Resource
    FollowMapper followMapper ;

    @Resource
    FollowerMapper followerMapper;

    @Resource
    UserSocialCounterServiceBiz userSocialCounterServiceBiz;

    @Resource
    RedisTemplate<String, String> redisTemplate;

    /**
     * 写入关注关系。
     *
     * @param dto 关注参数，followerId 表示关注者，followeeId 表示被关注者
     */
    @Override
    @Transactional
    public void follow(FollowDto dto) {
        verify(dto);
        String lockKey = buildFollowPairLockKey(dto.getFollowerId(), dto.getFolloweeId());
        try (RedisLock redisLock = RedisLockSupport.newLock(redisTemplate, FOLLOW_LOCK_MAX_HOLD_SECONDS)) {
            Status.FOLLOW_OPERATION_BUSY.assertThrowResEx(!redisLock.lock(lockKey, FOLLOW_LOCK_WAIT_MILLIS));
            if (existsFollow(dto.getFollowerId(), dto.getFolloweeId())) {
                return;
            }
            long id = FollowSupport.FOLLOW_ID_SNOWFLAKE.nextId();
            long time = com.clmcat.qianyu.core.snowflake.SnowflakeSupport.parseTimeBySnowflake(FollowSupport.FOLLOW_ID_SNOWFLAKE, id);
            boolean reverseFollow = existsFollow(dto.getFolloweeId(), dto.getFollowerId());
            // 关注
            Follow follow = new Follow();
            follow.setId(id);
            follow.setFollowerId(dto.getFollowerId());
            follow.setFolloweeId(dto.getFolloweeId());
            follow.setIsFriend(reverseFollow ? FollowSupport.FRIEND_YES : FollowSupport.FRIEND_NO);
            follow.setClientTime(time);

            // 粉丝
            Follower follower= new Follower();
            follower.setId(id);
            follower.setFollowerId(dto.getFollowerId());
            follower.setFolloweeId(dto.getFolloweeId());
            follower.setIsFriend(reverseFollow ? FollowSupport.FRIEND_YES : FollowSupport.FRIEND_NO);
            follower.setClientTime(time);

            try {
                followMapper.insert(follow);
                followerMapper.insert(follower);
                if (reverseFollow) {
                    // 双向关注后，双方在 follow / follower 两张表中的对应关系都要标记成好友。
                    updateFriendState(dto.getFollowerId(), dto.getFolloweeId(), FollowSupport.FRIEND_YES);
                    updateFriendState(dto.getFolloweeId(), dto.getFollowerId(), FollowSupport.FRIEND_YES);
                }
                userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                        .userId(dto.getFollowerId())
                        .followCount(1L)
                        .friendCount(reverseFollow ? 1L : null)
                        .build());
                userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                        .userId(dto.getFolloweeId())
                        .followerCount(1L)
                        .friendCount(reverseFollow ? 1L : null)
                        .build());
            }  catch (DuplicateKeyException e) {
                log.debug("follow already exists, followerId={}, followeeId={}", dto.getFollowerId(), dto.getFolloweeId());
            }
        }
    }

    /**
     * 取消关注这个人
     *
     * @param dto 取消关注参数，followerId 表示关注者，followeeId 表示被关注者
     */
    @Override
    @Transactional
    public void cancelFollow(FollowDto dto) {
        verify(dto);
        String lockKey = buildFollowPairLockKey(dto.getFollowerId(), dto.getFolloweeId());
        try (RedisLock redisLock = RedisLockSupport.newLock(redisTemplate, FOLLOW_LOCK_MAX_HOLD_SECONDS)) {
            Status.FOLLOW_OPERATION_BUSY.assertThrowResEx(!redisLock.lock(lockKey, FOLLOW_LOCK_WAIT_MILLIS));
            Follow currentFollow = findFollow(dto.getFollowerId(), dto.getFolloweeId());
            if (currentFollow == null) {
                // 可能已经取消了。所以没查到ID
                return;
            }
            boolean reverseFollow = existsFollow(dto.getFolloweeId(), dto.getFollowerId());
            boolean wasFriend = Objects.equals(currentFollow.getIsFriend(), FollowSupport.FRIEND_YES);

            followMapper.deleteById(currentFollow.getId());
            followerMapper.deleteById(currentFollow.getId());
            if (reverseFollow && wasFriend) {
                // 单向取消后，保留下来的反向关注记录不再是好友关系。
                updateFriendState(dto.getFolloweeId(), dto.getFollowerId(), FollowSupport.FRIEND_NO);
            }
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(dto.getFollowerId())
                    .followCount(-1L)
                    .friendCount(reverseFollow && wasFriend ? -1L : null)
                    .build());
            userSocialCounterServiceBiz.increment(UserSocialCounterDto.builder()
                    .userId(dto.getFolloweeId())
                    .followerCount(-1L)
                    .friendCount(reverseFollow && wasFriend ? -1L : null)
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
        boolean friend = existsFriend(userId, targetUserId) || existsFriend(targetUserId, userId);
        return FollowRelationDto.builder()
                .userId(userId)
                .targetUserId(targetUserId)
                .follow(follow)
                .follower(follower)
                .friend(friend)
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
    public FollowListDto getFollowListByFollowerId(long followerId, long nextId, int limit) {
        if (followerId <= 0 || limit <= 0) {
            return FollowListDto.EMPTY;
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(Follow::getFollowerId, followerId);
        queryWrapper.lt(Follow::getId, nextId);
        queryWrapper.orderBy(Follow::getId, false);
        queryWrapper.limit(limit);
        List<Follow> follows = followMapper.selectListByQuery(queryWrapper);
        List<FollowDto> list = FollowSupport.toFollowDtoListFromFollow(follows);
        return FollowListDto.builder().follows(list).build();
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
    public FollowListDto getFollowerListByFolloweeId(long followeeId, long nextId, int limit) {
        if (followeeId <= 0 || limit <= 0) {
            return FollowListDto.EMPTY;
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(Follower::getFolloweeId, followeeId);
        queryWrapper.lt(Follower::getId, nextId);
        queryWrapper.orderBy(Follower::getId, false);
        queryWrapper.limit(limit);
        List<Follower> followers = followerMapper.selectListByQuery(queryWrapper);
        List<FollowDto> list = FollowSupport.toFollowDtoListFromFollower(followers);
        return FollowListDto.builder().follows(list).build();
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

    /**
     * 查询用户的联系人（好友）ID 列表。
     *
     * @param userId 用户ID；好友从 follower 表按 followee_id=userId 且 is_friend=1 读取
     * @return 好友用户ID列表
     */
    @Override
    public List<Long> getFriendIdsByUserId(long userId) {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Follower::getFolloweeId, userId)
                .eq(Follower::getIsFriend, FollowSupport.FRIEND_YES)
                .orderBy(Follower::getId, false)
                .select(Follower::getFollowerId);
        return followerMapper.selectObjectListByQueryAs(queryWrapper, Long.class);
    }

    private boolean existsFollow(long followerId, long followeeId) {
        return findFollow(followerId, followeeId) != null;
    }

    private boolean existsFriend(long followerId, long followeeId) {
        Follow follow = findFollow(followerId, followeeId);
        return follow != null && Objects.equals(follow.getIsFriend(), FollowSupport.FRIEND_YES);
    }

    private Follow findFollow(long followerId, long followeeId) {
        if (followerId <= 0 || followeeId <= 0) {
            return null;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFolloweeId, followeeId);
        return followMapper.selectOneByQuery(queryWrapper);
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

    /**
     * 同步更新一条 follow 关系和它对应的 follower 关系的好友状态。
     *
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @param isFriend 是否互关好友：0否，1是
     */
    private void updateFriendState(long followerId, long followeeId, int isFriend) {
        followMapper.updateFriendState(followerId, followeeId, isFriend);
        followerMapper.updateFriendState(followeeId, followerId, isFriend);
    }

    /**
     * 为一组用户对生成同一把锁，保证 A->B 与 B->A 的互关写入可以串行执行。
     *
     * @param firstUserId 用户ID1
     * @param secondUserId 用户ID2
     * @return 用户对锁 key
     */
    private String buildFollowPairLockKey(long firstUserId, long secondUserId) {
        long minUserId = Math.min(firstUserId, secondUserId);
        long maxUserId = Math.max(firstUserId, secondUserId);
        return FOLLOW_LOCK_KEY_PREFIX + minUserId + ":" + maxUserId;
    }

    private void verify(FollowDto dto) {
        Status.FOLLOWER_REQUIRED.assertThrowResEx(dto == null || FollowSupport.isNullOrNonPositive(dto.getFollowerId()));
        Status.FOLLOWEE_REQUIRED.assertThrowResEx(dto == null || FollowSupport.isNullOrNonPositive(dto.getFolloweeId()));
        Status.FOLLOW_SELF_NOT_ALLOWED.assertThrowResEx(dto.getFollowerId().equals(dto.getFolloweeId()));
    }
}
