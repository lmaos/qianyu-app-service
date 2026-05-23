package com.clmcat.qianyu.user.model.dto;

import lombok.Data;

@Data
public class UserAuthDto {
    /**
     * 授权类型：phone/email/username/wechat/qq等
     */
    private String identityType;

    /**
     * 授权标识：手机号/邮箱/用户名/三方openid
     */
    private String identifier;

    /**
     * 凭证(密码/令牌，三方登录可为空)
     */
    private String credential;
    /**
     * 国家代码
     */
    private String country;

    /**
     * 头像URL
     */
    private String avatar;
}
