package com.clmcat.qianyu.user.support.login.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信登录配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "qianyu.login.wechat")
public class WechatLoginProperties {

    /**
     * 微信开放平台 / 公众号的 AppID。
     */
    private String appId;

    /**
     * 微信开放平台 / 公众号的 AppSecret。
     */
    private String appSecret;

    /**
     * 微信 access_token 接口地址（通过 code 换取 access_token 和 openid）。
     */
    private String tokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";

    /**
     * 微信用户信息接口地址（通过 access_token 和 openid 获取用户基本信息）。
     */
    private String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo";
}
