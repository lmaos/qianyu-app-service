package com.clmcat.qianyu.user.service;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import com.clmcat.qianyu.user.model.dto.UserIdDto;
import com.clmcat.qianyu.user.model.dto.UserIdsDto;
import com.clmcat.qianyu.user.model.vo.UserInfoVo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserViewServiceBiz {

    @Resource
    UserServiceBiz userServiceBiz;

    @Resource
    UserCacheServiceBiz userCacheServiceBiz;

    /**
     * 查询单个用户信息。
     *
     * @param viewerId 当前查看者ID；查询自己时不走缓存，查询别人时返回公开资料
     * @param dto 查询参数，targetId 表示要查询的目标用户ID
     * @return 用户信息 VO；查不到返回 null
     */
    public UserInfoVo getUserInfo(long viewerId, UserIdDto dto) {
        long targetId = dto == null ? 0L : Objects.requireNonNullElse(dto.getTargetId(), 0L);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(targetId <= 0);
        return toUserInfoVo(userCacheServiceBiz.getUserInfo(viewerId, targetId));
    }

    /**
     * 批量查询用户信息。
     *
     * @param viewerId 当前查看者ID；查询自己时不走缓存，查询别人时返回公开资料
     * @param dto 查询参数，支持 targetIds 数组或逗号分隔字符串
     * @return 用户信息列表
     */
    public List<UserInfoVo> getUserInfoList(long viewerId, UserIdsDto dto) {
        List<Long> targetIds = normalizeUserIds(dto);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(targetIds.isEmpty());
        List<RpcUserInfoDto> userInfoList = userCacheServiceBiz.getUserInfoList(viewerId, targetIds);
        return toUserInfoVoList(userInfoList);
    }

    /**
     * 查询当前登录用户自己的信息。
     *
     * @param userId 当前登录用户ID
     * @return 当前登录用户信息
     */
    public UserInfoVo getSelfUserInfo(long userId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        return toUserInfoVo(userServiceBiz.getUserInfo(userId));
    }

    private List<Long> normalizeUserIds(UserIdsDto dto) {
        List<Long> ids = new ArrayList<>();
        if (dto == null) {
            return ids;
        }
        if (dto.getTargetIds() != null) {
            for (Long targetId : dto.getTargetIds()) {
                if (targetId != null && targetId > 0) {
                    ids.add(targetId);
                }
            }
        }
        if (ids.isEmpty() && dto.getTargetIdsText() != null && !dto.getTargetIdsText().isBlank()) {
            String[] split = dto.getTargetIdsText().split(",");
            for (String part : split) {
                String text = part == null ? null : part.trim();
                if (text == null || text.isEmpty()) {
                    continue;
                }
                ResponseStatus.P_VALUE_ERROR.assertThrowResEx(!text.matches("\\d+"));
                long targetId = Long.parseLong(text);
                if (targetId > 0) {
                    ids.add(targetId);
                }
            }
        }
        return ids;
    }

    private UserInfoVo toUserInfoVo(RpcUserInfoDto dto) {
        if (dto == null) {
            return null;
        }
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }

    private List<UserInfoVo> toUserInfoVoList(List<RpcUserInfoDto> dtos) {
        List<UserInfoVo> list = new ArrayList<>();
        if (dtos == null) {
            return list;
        }
        for (RpcUserInfoDto dto : dtos) {
            UserInfoVo vo = toUserInfoVo(dto);
            if (vo != null) {
                list.add(vo);
            }
        }
        return list;
    }
}
