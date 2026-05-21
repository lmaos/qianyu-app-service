package com.clmcat.qianyu.user.model.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResultVo {

    private String token;
    /**
     * 用户的基本信息
     */
    private UserInfoVo userInfo;
}
