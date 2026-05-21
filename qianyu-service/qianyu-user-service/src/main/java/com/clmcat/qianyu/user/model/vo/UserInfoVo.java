package com.clmcat.qianyu.user.model.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoVo {

    /**
     * 登录的用户编号, 不为空
     */
    private String userNo;
    /**
     * 登录时使用的用户ID，某些时候不存在
     */
    private Long userId;
    /**
     * 昵称
     */
    private String nickname;
}
