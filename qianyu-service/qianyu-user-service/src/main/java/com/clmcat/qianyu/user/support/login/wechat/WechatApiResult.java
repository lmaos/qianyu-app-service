package com.clmcat.qianyu.user.support.login.wechat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信登录 API 返回结果。
 * <p>
 * 成功响应：
 * <pre>
 * {
 *   "access_token": "ACCESS_TOKEN",
 *   "expires_in": 7200,
 *   "refresh_token": "REFRESH_TOKEN",
 *   "openid": "OPENID",
 *   "scope": "SCOPE",
 *   "unionid": "UNIONID"
 * }
 * </pre>
 * 错误响应：
 * <pre>
 * {
 *   "errcode": 40029,
 *   "errmsg": "invalid code"
 * }
 * </pre>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WechatApiResult {

    /** 接口调用凭证 */
    @JsonProperty("access_token")
    private String accessToken;

    /** 凭证有效时间，单位：秒 */
    @JsonProperty("expires_in")
    private Integer expiresIn;

    /** 刷新凭证 */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /** 用户唯一标识（在当前 AppID 下） */
    @JsonProperty("openid")
    private String openid;

    /** 用户授权的作用域 */
    @JsonProperty("scope")
    private String scope;

    /** 用户全局标识（仅在开放平台且满足条件时返回） */
    @JsonProperty("unionid")
    private String unionid;

    // ---- 用户信息（通过 userinfo 接口获取） ----
    /** 用户昵称 */
    private String nickname;

    /** 用户头像 URL */
    @JsonProperty("headimgurl")
    private String headimgurl;

    /** 用户性别：1-男，2-女，0-未知 */
    private Integer sex;

    /** 用户所在国家 */
    private String country;

    /** 用户所在省份 */
    private String province;

    /** 用户所在城市 */
    private String city;

    // ---- 错误响应 ----
    @JsonProperty("errcode")
    private Integer errcode;

    @JsonProperty("errmsg")
    private String errmsg;

    /**
     * 判断微信 API 是否返回了业务错误。
     */
    public boolean isError() {
        return errcode != null && errcode != 0;
    }
}
