package com.clmcat.qianyu.social.follow.service;

import com.clmcat.qianyu.social.api.follow.model.dto.FollowCountDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowRelationDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowListQueryDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowTargetDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowUserQueryDto;
import com.clmcat.qianyu.social.follow.model.entity.status.Status;
import com.clmcat.qianyu.social.follow.model.vo.FollowCountVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowPageVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowRelationVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowUserVo;
import com.clmcat.qianyu.social.follow.support.FollowSupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class FollowViewServiceBiz  {

    @Resource
    FollowServiceBiz followServiceBiz;

    /**
     * 当前用户发起关注。
     *
     * @param userId 当前登录用户ID
     * @param dto 目标用户参数，targetId 表示要关注的人
     * @return 是否处理成功
     */
    public boolean follow(long userId, FollowTargetDto dto) {
        FollowDto followDto = FollowSupport.newFollowDto(userId, dto);
        verifyTarget(followDto);
        followServiceBiz.follow(followDto);
        return true;
    }

    /**
     * 当前用户取消关注。
     *
     * @param userId 当前登录用户ID
     * @param dto 目标用户参数，targetId 表示要取消关注的人
     * @return 是否处理成功
     */
    public boolean cancelFollow(long userId, FollowTargetDto dto) {
        FollowDto followDto = FollowSupport.newFollowDto(userId, dto);
        verifyTarget(followDto);
        followServiceBiz.cancelFollow(followDto);
        return true;
    }

    /**
     * 查询当前用户与目标用户的关系。
     *
     * @param userId 当前登录用户ID
     * @param dto 目标用户参数，targetId 表示对方用户
     * @return 关系 VO
     */
    public FollowRelationVo getRelation(long userId, FollowTargetDto dto) {
        Status.FOLLOWER_REQUIRED.assertThrowResEx(FollowSupport.isNullOrNonPositive(userId));
        long targetUserId = dto == null ? 0L : Objects.requireNonNullElse(dto.getTargetId(), 0L);
        Status.FOLLOWEE_REQUIRED.assertThrowResEx(FollowSupport.isNullOrNonPositive(targetUserId));
        Status.FOLLOW_SELF_NOT_ALLOWED.assertThrowResEx(userId == targetUserId);

        FollowRelationDto relationDto = followServiceBiz.getRelation(userId, targetUserId);
        return FollowSupport.toFollowRelationVo(relationDto);
    }

    /**
     * 查询某个用户的关注列表。
     *
     * @param dto 查询参数，userId 表示被查询用户，nextId 为游标，limit 为分页大小
     * @return 关注列表分页 VO
     */
    public FollowPageVo getFollowList(FollowListQueryDto dto) {
        long userId = dto == null ? 0L : Objects.requireNonNullElse(dto.getUserId(), 0L);
        Status.QUERY_USER_REQUIRED.assertThrowResEx(FollowSupport.isNullOrNonPositive(userId));

        int limit = FollowSupport.normalizeLimit(dto == null ? null : dto.getLimit());
        long nextId = FollowSupport.normalizeCursorId(dto == null ? null : dto.getNextId());
        List<FollowDto> followDtos = followServiceBiz.getFollowListByFollowerId(userId, nextId, limit + 1);

        boolean hasMore = followDtos.size() > limit;
        if (hasMore) {
            followDtos = new ArrayList<>(followDtos.subList(0, limit));
        }
        long nextCursorId = hasMore && !followDtos.isEmpty() ? followDtos.get(followDtos.size() - 1).getId() : 0L;
        List<FollowUserVo> userList = FollowSupport.toFolloweeVoList(followDtos);
        return FollowSupport.newFollowPageVo(userId, hasMore, nextCursorId, userList);
    }

    /**
     * 查询某个用户的粉丝列表。
     *
     * @param dto 查询参数，userId 表示被查询用户，nextId 为游标，limit 为分页大小
     * @return 粉丝列表分页 VO
     */
    public FollowPageVo getFollowerList(FollowListQueryDto dto) {
        long userId = dto == null ? 0L : Objects.requireNonNullElse(dto.getUserId(), 0L);
        Status.QUERY_USER_REQUIRED.assertThrowResEx(FollowSupport.isNullOrNonPositive(userId));

        int limit = FollowSupport.normalizeLimit(dto == null ? null : dto.getLimit());
        long nextId = FollowSupport.normalizeCursorId(dto == null ? null : dto.getNextId());
        List<FollowDto> followDtos = followServiceBiz.getFollowerListByFolloweeId(userId, nextId, limit + 1);

        boolean hasMore = followDtos.size() > limit;
        if (hasMore) {
            followDtos = new ArrayList<>(followDtos.subList(0, limit));
        }
        long nextCursorId = hasMore && !followDtos.isEmpty() ? followDtos.get(followDtos.size() - 1).getId() : 0L;
        List<FollowUserVo> userList = FollowSupport.toFollowerVoList(followDtos);
        return FollowSupport.newFollowPageVo(userId, hasMore, nextCursorId, userList);
    }

    /**
     * 查询某个用户的关注数与粉丝数。
     *
     * @param dto 查询参数，userId 表示被查询用户
     * @return 数量 VO
     */
    public FollowCountVo getFollowCount(FollowUserQueryDto dto) {
        long userId = dto == null ? 0L : Objects.requireNonNullElse(dto.getUserId(), 0L);
        Status.QUERY_USER_REQUIRED.assertThrowResEx(FollowSupport.isNullOrNonPositive(userId));

        FollowCountDto countDto = followServiceBiz.getFollowCount(userId);
        return FollowSupport.toFollowCountVo(countDto);
    }

    private void verifyTarget(FollowDto followDto) {
        Status.FOLLOWER_REQUIRED.assertThrowResEx(followDto == null || FollowSupport.isNullOrNonPositive(followDto.getFollowerId()));
        Status.FOLLOWEE_REQUIRED.assertThrowResEx(followDto == null || FollowSupport.isNullOrNonPositive(followDto.getFolloweeId()));
        Status.FOLLOW_SELF_NOT_ALLOWED.assertThrowResEx(followDto.getFollowerId().equals(followDto.getFolloweeId()));
    }
}
