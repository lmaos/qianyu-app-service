package com.clmcat.qianyu.user.api;

import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;

import java.util.Collection;
import java.util.List;

/**
 * 用户 API
 */
public interface UserApi {
    /**
     * 查询用户信息， 查询不到返回NULL
     * @param userId 用户ID
     * @return 用户信息
     */
    RpcUserInfoDto  getUserInfo(long userId);

    /**
     * 查询用户信息
     * @param userIds 用户ID 集合
     * @return 用户信息 集合
     */
    List<RpcUserInfoDto> getUserInfoList(Collection<Long> userIds);

}
