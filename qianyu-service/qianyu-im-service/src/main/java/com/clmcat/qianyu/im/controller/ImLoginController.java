package com.clmcat.qianyu.im.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.im.api.model.dto.ImLoginDto;
import com.clmcat.qianyu.im.api.model.dto.ImLoginResultDto;
import com.clmcat.qianyu.im.api.model.dto.ImRefreshDto;
import com.clmcat.qianyu.im.service.ImLoginServiceBiz;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * IM 登录接口
 * POST /api/im/login  — 获取 IM 登录凭证
 * POST /api/im/refresh — 刷新 IM 登录凭证
 */
@Slf4j
@ApiController
@RequestMapping("/api/im")
public class ImLoginController {

    @Resource
    private ImLoginServiceBiz imLoginServiceBiz;

    /**
     * 获取 IM 登录凭证
     * 请求体: { "channel": "tencent", "userId": 10001 }
     * 响应: { "imToken": "xxx", "channel": "tencent", "sdkAppId": 1600143689 }
     */
    @RequestMapping("/login")
    public ImLoginResultDto login(@Params ImLoginDto dto, @Token long userId) {
        long effectiveUserId = resolveUserId(userId, dto != null ? dto.getUserId() : null);
        log.info("IM 登录请求: userId={}, channel={}", effectiveUserId, dto != null ? dto.getChannel() : null);
        return imLoginServiceBiz.login(effectiveUserId, dto);
    }

    /**
     * 刷新 IM 登录凭证
     * 请求体: { "channel": "tencent", "userId": 10001 }
     * 响应: { "imToken": "xxx", "channel": "tencent", "sdkAppId": 1600143689 }
     */
    @RequestMapping("/refresh")
    public ImLoginResultDto refresh(@Params ImRefreshDto dto, @Token long userId) {
        long effectiveUserId = resolveUserId(userId, dto != null ? dto.getUserId() : null);
        log.info("IM 刷新请求: userId={}, channel={}", effectiveUserId, dto != null ? dto.getChannel() : null);
        return imLoginServiceBiz.refresh(effectiveUserId, dto);
    }

    /**
     * 优先使用 @Token，为 0 时降级使用请求体中的 userId
     */
    private long resolveUserId(long tokenUserId, Long bodyUserId) {
        if (tokenUserId > 0) return tokenUserId;
        if (bodyUserId != null && bodyUserId > 0) return bodyUserId;
        return tokenUserId;
    }
}
