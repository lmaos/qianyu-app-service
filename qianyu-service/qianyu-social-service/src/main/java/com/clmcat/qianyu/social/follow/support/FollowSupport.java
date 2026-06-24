package com.clmcat.qianyu.social.follow.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowCountDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowDto;
import com.clmcat.qianyu.social.api.follow.model.dto.FollowRelationDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowTargetDto;
import com.clmcat.qianyu.social.follow.model.entity.Follow;
import com.clmcat.qianyu.social.follow.model.entity.Follower;
import com.clmcat.qianyu.social.follow.model.vo.FollowCountVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowPageVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowRelationVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowUserVo;

import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FollowSupport {
    public static final int FRIEND_NO = 0;
    public static final int FRIEND_YES = 1;
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    public static final CustomSnowflake FOLLOW_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    public static FollowDto newFollowDto(long followerId, FollowTargetDto dto) {
        if (dto == null) {
            return null;
        }
        return FollowDto.builder()
                .followerId(followerId)
                .followeeId(dto.getTargetId())
                .build();
    }

    public static FollowDto toFollowDto(Follow follow) {
        if (follow == null) {
            return null;
        }
        return FollowDto.builder()
                .id(follow.getId())
                .followerId(follow.getFollowerId())
                .followeeId(follow.getFolloweeId())
                .isFriend(follow.getIsFriend())
                .clientTime(follow.getClientTime())
                .build();
    }

    public static FollowDto toFollowDto(Follower follower) {
        if (follower == null) {
            return null;
        }
        return FollowDto.builder()
                .id(follower.getId())
                .followerId(follower.getFollowerId())
                .followeeId(follower.getFolloweeId())
                .isFriend(follower.getIsFriend())
                .clientTime(follower.getClientTime())
                .build();
    }

    public static List<FollowDto> toFollowDtoListFromFollow(Collection<Follow> follows) {
        List<FollowDto> list = new ArrayList<>();
        if (follows == null) {
            return list;
        }
        for (Follow follow : follows) {
            FollowDto dto = toFollowDto(follow);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }

    public static List<FollowDto> toFollowDtoListFromFollower(Collection<Follower> followers) {
        List<FollowDto> list = new ArrayList<>();
        if (followers == null) {
            return list;
        }
        for (Follower follower : followers) {
            FollowDto dto = toFollowDto(follower);
            if (dto != null) {
                list.add(dto);
            }
        }
        return list;
    }

    public static FollowUserVo toFolloweeVo(FollowDto dto) {
        return toFolloweeVo(dto, null);
    }

    public static FollowUserVo toFolloweeVo(FollowDto dto, Map<Long, RpcUserInfoDto> userMap) {
        if (dto == null) {
            return null;
        }
        RpcUserInfoDto userInfo = userMap != null ? userMap.get(dto.getFolloweeId()) : null;
        return FollowUserVo.builder()
                .id(dto.getId())
                .userId(dto.getFolloweeId())
                .isFriend(dto.getIsFriend())
                .clientTime(dto.getClientTime())
                .nickname(userInfo != null ? userInfo.getNickname() : null)
                .avatar(userInfo != null ? userInfo.getAvatar() : null)
                .build();
    }

    public static FollowUserVo toFollowerVo(FollowDto dto) {
        return toFollowerVo(dto, null);
    }

    public static FollowUserVo toFollowerVo(FollowDto dto, Map<Long, RpcUserInfoDto> userMap) {
        if (dto == null) {
            return null;
        }
        RpcUserInfoDto userInfo = userMap != null ? userMap.get(dto.getFollowerId()) : null;
        return FollowUserVo.builder()
                .id(dto.getId())
                .userId(dto.getFollowerId())
                .isFriend(dto.getIsFriend())
                .clientTime(dto.getClientTime())
                .nickname(userInfo != null ? userInfo.getNickname() : null)
                .avatar(userInfo != null ? userInfo.getAvatar() : null)
                .build();
    }

    public static List<FollowUserVo> toFolloweeVoList(Collection<FollowDto> follows) {
        return toFolloweeVoList(follows, null);
    }

    public static List<FollowUserVo> toFolloweeVoList(Collection<FollowDto> follows, Map<Long, RpcUserInfoDto> userMap) {
        List<FollowUserVo> list = new ArrayList<>();
        if (follows == null) {
            return list;
        }
        for (FollowDto dto : follows) {
            FollowUserVo vo = toFolloweeVo(dto, userMap);
            if (vo != null) {
                list.add(vo);
            }
        }
        return list;
    }

    public static List<FollowUserVo> toFollowerVoList(Collection<FollowDto> follows) {
        return toFollowerVoList(follows, null);
    }

    public static List<FollowUserVo> toFollowerVoList(Collection<FollowDto> follows, Map<Long, RpcUserInfoDto> userMap) {
        List<FollowUserVo> list = new ArrayList<>();
        if (follows == null) {
            return list;
        }
        for (FollowDto dto : follows) {
            FollowUserVo vo = toFollowerVo(dto, userMap);
            if (vo != null) {
                list.add(vo);
            }
        }
        return list;
    }

    public static FollowRelationVo toFollowRelationVo(FollowRelationDto dto) {
        if (dto == null) {
            return null;
        }
        return FollowRelationVo.builder()
                .userId(dto.getUserId())
                .targetUserId(dto.getTargetUserId())
                .follow(dto.isFollow())
                .follower(dto.isFollower())
                .friend(dto.isFriend())
                .build();
    }

    public static FollowCountVo toFollowCountVo(FollowCountDto dto) {
        if (dto == null) {
            return null;
        }
        return FollowCountVo.builder()
                .userId(dto.getUserId())
                .followCount(dto.getFollowCount())
                .followerCount(dto.getFollowerCount())
                .build();
    }

    public static FollowPageVo newFollowPageVo(long userId, boolean hasMore, long nextId, List<FollowUserVo> followList) {
        return FollowPageVo.builder()
                .userId(userId)
                .hasMore(hasMore)
                .nextId(nextId)
                .followList(followList)
                .build();
    }

    public static int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    public static long normalizeCursorId(Long nextId) {
        if (isNullOrNonPositive(nextId)) {
            return Long.MAX_VALUE;
        }
        return nextId;
    }

    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.longValue() <= 0;
    }
}
