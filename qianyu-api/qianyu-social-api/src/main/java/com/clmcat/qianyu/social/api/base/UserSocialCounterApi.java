package com.clmcat.qianyu.social.api.base;

import com.clmcat.qianyu.social.api.base.model.dto.UserSocialCounterDto;

public interface UserSocialCounterApi {

    void increment(UserSocialCounterDto dto);

    void init(long userId);

    /**
     * 查询用户社交统计数据
     *
     * @param userId 用户ID
     * @return 用户社交统计数据；不存在返回 null
     */
    UserSocialCounterDto getByUserId(long userId);
}
