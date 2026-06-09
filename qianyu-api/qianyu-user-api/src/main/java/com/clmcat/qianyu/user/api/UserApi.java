package com.clmcat.qianyu.user.api;

import com.clmcat.qianyu.user.api.model.dto.PpcUserInfoListDto;
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
    PpcUserInfoListDto getUserInfoList(Collection<Long> userIds);

    /**
     * 按 userNo 精确查询用户。userNo 全局唯一（UNIQUE KEY uk_user_no），
     * 最多返回 1 条；未命中返回 null。
     *
     * @param userNo 用户外显 ID
     * @return 命中的用户信息；未命中返回 null
     */
    RpcUserInfoDto getUserInfoByUserNo(String userNo);

}
