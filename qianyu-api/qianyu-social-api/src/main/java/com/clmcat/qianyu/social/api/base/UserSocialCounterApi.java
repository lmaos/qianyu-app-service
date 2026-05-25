package com.clmcat.qianyu.social.api.base;

import com.clmcat.qianyu.social.api.base.model.dto.UserSocialCounterDto;

public interface UserSocialCounterApi {

    void increment(UserSocialCounterDto dto);

    void init(long userId);
}
