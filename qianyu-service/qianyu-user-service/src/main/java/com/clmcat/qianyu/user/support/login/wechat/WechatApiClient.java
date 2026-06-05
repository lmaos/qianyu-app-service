package com.clmcat.qianyu.user.support.login.wechat;

import com.clmcat.framework.webmvc.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 微信 API 客户端，封装与微信服务器的 HTTP 通信。
 */
@Slf4j
@Component
public class WechatApiClient {

    private final WechatLoginProperties properties;
    private final RestTemplate restTemplate;

    public WechatApiClient(WechatLoginProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 通过授权 code 换取 access_token 和 openid。
     * <p>
     * 请求微信接口：
     * GET https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
     *
     * @param code 前端授权后拿到的 code
     * @return 微信 API 返回结果（含 openid）
     */
    public WechatApiResult getAccessToken(String code) {
        String url = String.format(
                "%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                properties.getTokenUrl(),
                properties.getAppId(),
                properties.getAppSecret(),
                code
        );
        log.debug("请求微信 access_token, appId={}", properties.getAppId());
        return doGet(url);
    }

    /**
     * 通过 access_token 和 openid 获取微信用户基本信息。
     * <p>
     * GET https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
     *
     * @param accessToken 调用 getAccessToken 获取的 access_token
     * @param openid      用户的 openid
     * @return 微信用户信息（含昵称、头像、性别等）
     */
    public WechatApiResult getUserInfo(String accessToken, String openid) {
        String url = String.format(
                "%s?access_token=%s&openid=%s",
                properties.getUserInfoUrl(),
                accessToken,
                openid
        );
        log.debug("请求微信用户信息, openid={}", openid);
        return doGet(url);
    }

    private WechatApiResult doGet(String url) {
        WechatApiResult result;
        try {
            result = restTemplate.getForObject(url, WechatApiResult.class);
        } catch (Exception e) {
            log.error("微信 API 调用异常, url={}", maskUrl(url), e);
            throw ResponseStatus.F_SERVICE_UNAVAILABLE.apiEx("微信登录服务暂不可用");
        }

        if (result == null) {
            log.error("微信 API 返回空响应");
            throw ResponseStatus.F_SERVICE_UNAVAILABLE.apiEx("微信登录服务暂不可用");
        }

        if (result.isError()) {
            log.error("微信 API 返回错误, errcode={}, errmsg={}", result.getErrcode(), result.getErrmsg());
            throw ResponseStatus.AUTH_LOGIN_FAIL.apiEx("微信登录失败: " + result.getErrmsg());
        }

        return result;
    }

    /**
     * 脱敏 URL（隐藏 secret 参数），仅用于日志打印。
     */
    private String maskUrl(String url) {
        return url.replaceAll("secret=[^&]+", "secret=****");
    }
}
