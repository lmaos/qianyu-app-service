package com.clmcat.qianyu.user.service;

import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 用户信息查询缓存：
 * 1. 查询别人时走本地缓存；
 * 2. 查询自己时直接查服务，不走缓存；
 * 3. 缓存中只保留对外公开的用户资料。
 */
@Service
public class UserCacheServiceBiz {
    private static final Duration USER_INFO_CACHE_TTL = Duration.ofMinutes(2);

    private final Cache<Long, RpcUserInfoDto> otherUserInfoCache = Caffeine.newBuilder()
            .maximumSize(2_048)
            .expireAfterWrite(USER_INFO_CACHE_TTL)
            .build();

    @Resource
    private UserServiceBiz userServiceBiz;

    /**
     * 查询单个用户信息；本人查询自己时绕过缓存。
     *
     * @param viewerId 当前查看者ID
     * @param targetId 目标用户ID
     * @return 用户信息
     */
    public RpcUserInfoDto getUserInfo(long viewerId, long targetId) {
        if (Objects.equals(viewerId, targetId)) {
            return userServiceBiz.getUserInfo(targetId);
        }

        RpcUserInfoDto cachedUserInfo = otherUserInfoCache.getIfPresent(targetId);
        if (cachedUserInfo != null) {
            return copyUserInfo(cachedUserInfo);
        }

        RpcUserInfoDto userInfoDto = userServiceBiz.getUserInfo(targetId);
        if (userInfoDto == null) {
            otherUserInfoCache.invalidate(targetId);
            return null;
        }

        RpcUserInfoDto publicUserInfo = toPublicUserInfo(userInfoDto);
        otherUserInfoCache.put(targetId, publicUserInfo);
        return copyUserInfo(publicUserInfo);
    }

    /**
     * 批量查询用户信息；当前登录用户自己的资料不走缓存，其他用户信息走公开资料缓存。
     *
     * @param viewerId 当前查看者ID
     * @param targetIds 目标用户ID集合
     * @return 用户信息列表
     */
    public List<RpcUserInfoDto> getUserInfoList(long viewerId, Collection<Long> targetIds) {
        List<RpcUserInfoDto> result = new ArrayList<>();
        if (targetIds == null || targetIds.isEmpty()) {
            return result;
        }

        Map<Long, RpcUserInfoDto> userInfoMap = new HashMap<>();
        Set<Long> missIds = new LinkedHashSet<>();
        for (Long targetId : targetIds) {
            if (targetId == null) {
                continue;
            }
            if (Objects.equals(viewerId, targetId)) {
                userInfoMap.put(targetId, userServiceBiz.getUserInfo(targetId));
                continue;
            }
            RpcUserInfoDto cachedUserInfo = otherUserInfoCache.getIfPresent(targetId);
            if (cachedUserInfo != null) {
                userInfoMap.put(targetId, cachedUserInfo);
                continue;
            }
            missIds.add(targetId);
        }

        if (!missIds.isEmpty()) {
            List<RpcUserInfoDto> loadedUserInfos = userServiceBiz.getUserInfoList(missIds).getUsers();
            for (RpcUserInfoDto loadedUserInfo : loadedUserInfos) {
                if (loadedUserInfo == null || loadedUserInfo.getUserId() == null) {
                    continue;
                }
                RpcUserInfoDto publicUserInfo = toPublicUserInfo(loadedUserInfo);
                userInfoMap.put(publicUserInfo.getUserId(), publicUserInfo);
                otherUserInfoCache.put(publicUserInfo.getUserId(), publicUserInfo);
            }
        }

        for (Long targetId : targetIds) {
            RpcUserInfoDto userInfoDto = userInfoMap.get(targetId);
            if (userInfoDto != null) {
                result.add(copyUserInfo(userInfoDto));
            }
        }
        return result;
    }

    /**
     * 失效指定用户信息缓存。
     *
     * @param userId 用户ID
     */
    public void evictUserInfo(long userId) {
        otherUserInfoCache.invalidate(userId);
    }

    private RpcUserInfoDto toPublicUserInfo(RpcUserInfoDto dto) {
        RpcUserInfoDto publicUserInfo = copyUserInfo(dto);
        publicUserInfo.setPhone(null);
        publicUserInfo.setPhoneVerifiedTime(null);
        publicUserInfo.setEmail(null);
        publicUserInfo.setBirthday(null);
        publicUserInfo.setLastLoginTime(null);
        publicUserInfo.setFreezeEndTime(null);
        publicUserInfo.setCreateTime(null);
        publicUserInfo.setUpdateTime(null);
        return publicUserInfo;
    }

    private RpcUserInfoDto copyUserInfo(RpcUserInfoDto dto) {
        if (dto == null) {
            return null;
        }
        RpcUserInfoDto copy = new RpcUserInfoDto();
        BeanUtils.copyProperties(dto, copy);
        return copy;
    }
}
