package com.clmcat.qianyu.user.service;

import com.clmcat.qianyu.user.api.model.dto.AuthPlatform;
import com.clmcat.qianyu.user.model.dto.UserAuthDto;
import com.clmcat.qianyu.user.support.login.wechat.WechatApiClient;
import com.clmcat.qianyu.user.support.login.wechat.WechatApiResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 微信登录业务服务。
 * <p>
 * 通过微信授权 code 换取 openid，封装为 {@link UserAuthDto} 返回给
 * {@link UserLoginServiceBiz} 走通用登录/注册流程。
 */
@Slf4j
@Service
public class WeChatLoginServiceBiz {

    @Resource
    private WechatApiClient wechatApiClient;

    /**
     * 用微信授权 code 换取 UserAuthDto（含 openid、昵称、头像）。
     * <p>
     * 流程：
     * <ol>
     *   <li>调用微信 API 用 code 换取 access_token + openid</li>
     *   <li>获取微信用户基本信息（昵称、头像）</li>
     *   <li>封装为 UserAuthDto 返回</li>
     * </ol>
     *
     * @param code 前端微信授权后拿到的 code（一次有效）
     * @return UserAuthDto（identifier=openid, identityType=WECHAT）
     */
    public UserAuthDto getWechatUserAuthDto(String code) {
        // 1. 通过 code 换取 access_token 和 openid
        WechatApiResult tokenResult = wechatApiClient.getAccessToken(code);
        String openid = tokenResult.getOpenid();
        log.info("微信登录成功获取 openid, openid={}", openid);

        // 2. 获取微信用户基本信息（昵称、头像等），失败不影响登录
        String nickname = null;
        String avatar = null;
        try {
            WechatApiResult userInfo = wechatApiClient.getUserInfo(tokenResult.getAccessToken(), openid);
            nickname = userInfo.getNickname();
            avatar = userInfo.getHeadimgurl();
            log.debug("微信登录获取用户信息, nickname={}, avatar={}", nickname, avatar);
        } catch (Exception e) {
            log.warn("获取微信用户信息失败，仅使用 openid 登录, openid={}", openid, e);
        }

        // 3. 返回 UserAuthDto，由 UserLoginServiceBiz 走通用登录/注册
        return UserAuthDto.builder()
                .identifier(openid)
                .identityType(AuthPlatform.WECHAT.name())
                .socialNickname(nickname != null ? StringUtils.substring(nickname, 0, 64) : null)
                .socialAvatar(avatar)
                .country("CN")
                .build();
    }
}
