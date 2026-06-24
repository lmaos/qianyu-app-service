package com.clmcat.qianyu.social.follow.service;

import com.clmcat.qianyu.social.api.follow.model.dto.FollowCountDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowRelationDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowListQueryDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowSelfListQueryDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowTargetDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowUserQueryDto;
import com.clmcat.qianyu.social.follow.model.entity.status.Status;
import com.clmcat.qianyu.social.follow.model.vo.FollowCountVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowPageVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowRelationVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowUserVo;
import com.clmcat.qianyu.social.follow.support.FollowSupport;
import com.clmcat.qianyu.user.api.UserApi;
import com.clmcat.qianyu.user.api.model.dto.PpcUserInfoListDto;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FollowViewServiceBiz  {

    @Resource
    FollowServiceBiz followServiceBiz;

    @Resource
    UserApi userApi;

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
        List<FollowDto> followDtos = followServiceBiz.getFollowListByFollowerId(userId, nextId, limit + 1).getFollows();

        boolean hasMore = followDtos.size() > limit;
        if (hasMore) {
            followDtos = new ArrayList<>(followDtos.subList(0, limit));
        }
        long nextCursorId = hasMore && !followDtos.isEmpty() ? followDtos.get(followDtos.size() - 1).getId() : 0L;
        Map<Long, RpcUserInfoDto> userMap = queryUserInfoMapByFolloweeIds(followDtos);
        List<FollowUserVo> userList = FollowSupport.toFolloweeVoList(followDtos, userMap);
        return FollowSupport.newFollowPageVo(userId, hasMore, nextCursorId, userList);
    }

    /**
     * 查询当前登录用户自己的关注列表。
     *
     * @param userId 当前登录用户ID
     * @param dto 查询参数，包含 nextId、limit
     * @return 关注列表分页 VO
     */
    public FollowPageVo getSelfFollowList(long userId, FollowSelfListQueryDto dto) {
        Status.QUERY_USER_REQUIRED.assertThrowResEx(FollowSupport.isNullOrNonPositive(userId));
        return getFollowList(newSelfFollowListQuery(userId, dto));
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
        List<FollowDto> followDtos = followServiceBiz.getFollowerListByFolloweeId(userId, nextId, limit + 1).getFollows();

        boolean hasMore = followDtos.size() > limit;
        if (hasMore) {
            followDtos = new ArrayList<>(followDtos.subList(0, limit));
        }
        long nextCursorId = hasMore && !followDtos.isEmpty() ? followDtos.get(followDtos.size() - 1).getId() : 0L;
        Map<Long, RpcUserInfoDto> userMap = queryUserInfoMapByFollowerIds(followDtos);
        List<FollowUserVo> userList = FollowSupport.toFollowerVoList(followDtos, userMap);
        return FollowSupport.newFollowPageVo(userId, hasMore, nextCursorId, userList);
    }

    /**
     * 查询当前登录用户自己的粉丝列表。
     *
     * @param userId 当前登录用户ID
     * @param dto 查询参数，包含 nextId、limit
     * @return 粉丝列表分页 VO
     */
    public FollowPageVo getSelfFollowerList(long userId, FollowSelfListQueryDto dto) {
        Status.QUERY_USER_REQUIRED.assertThrowResEx(FollowSupport.isNullOrNonPositive(userId));
        return getFollowerList(newSelfFollowListQuery(userId, dto));
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

    /**
     * 查询当前登录用户自己的关注数与粉丝数。
     *
     * @param userId 当前登录用户ID
     * @return 数量 VO
     */
    public FollowCountVo getSelfFollowCount(long userId) {
        Status.QUERY_USER_REQUIRED.assertThrowResEx(FollowSupport.isNullOrNonPositive(userId));
        FollowUserQueryDto dto = new FollowUserQueryDto();
        dto.setUserId(userId);
        return getFollowCount(dto);
    }

    private void verifyTarget(FollowDto followDto) {
        Status.FOLLOWER_REQUIRED.assertThrowResEx(followDto == null || FollowSupport.isNullOrNonPositive(followDto.getFollowerId()));
        Status.FOLLOWEE_REQUIRED.assertThrowResEx(followDto == null || FollowSupport.isNullOrNonPositive(followDto.getFolloweeId()));
        Status.FOLLOW_SELF_NOT_ALLOWED.assertThrowResEx(followDto.getFollowerId().equals(followDto.getFolloweeId()));
    }

    private FollowListQueryDto newSelfFollowListQuery(long userId, FollowSelfListQueryDto dto) {
        FollowListQueryDto queryDto = new FollowListQueryDto();
        queryDto.setUserId(userId);
        if (dto != null) {
            queryDto.setNextId(dto.getNextId());
            queryDto.setLimit(dto.getLimit());
        }
        return queryDto;
    }

    /**
     * 批量查询关注列表中的用户信息（关注的是谁）。
     *
     * @param followDtos 关注关系列表
     * @return followeeId → RpcUserInfoDto 映射
     */
    private Map<Long, RpcUserInfoDto> queryUserInfoMapByFolloweeIds(List<FollowDto> followDtos) {
        if (followDtos == null || followDtos.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> userIds = followDtos.stream()
                .map(FollowDto::getFolloweeId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        return queryUserInfoMap(userIds);
    }

    /**
     * 批量查询粉丝列表中的用户信息（谁关注了 ta）。
     *
     * @param followDtos 粉丝关系列表
     * @return followerId → RpcUserInfoDto 映射
     */
    private Map<Long, RpcUserInfoDto> queryUserInfoMapByFollowerIds(List<FollowDto> followDtos) {
        if (followDtos == null || followDtos.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> userIds = followDtos.stream()
                .map(FollowDto::getFollowerId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        return queryUserInfoMap(userIds);
    }

    /**
     * 通过 UserApi 批量查询用户信息（userId → nickname/avatar）。
     */
    private Map<Long, RpcUserInfoDto> queryUserInfoMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            PpcUserInfoListDto userList = userApi.getUserInfoList(userIds);
            if (userList == null || userList.getUsers() == null) {
                return Collections.emptyMap();
            }
            Map<Long, RpcUserInfoDto> map = new HashMap<>(userList.getUsers().size());
            for (RpcUserInfoDto user : userList.getUsers()) {
                if (user != null && user.getUserId() != null) {
                    map.put(user.getUserId(), user);
                }
            }
            return map;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
