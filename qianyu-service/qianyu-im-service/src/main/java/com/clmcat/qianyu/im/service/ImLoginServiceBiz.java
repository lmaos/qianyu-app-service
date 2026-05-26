package com.clmcat.qianyu.im.service;

import com.clmcat.qianyu.im.api.ImLoginApi;
import com.clmcat.qianyu.im.api.model.dto.ImLoginDto;
import com.clmcat.qianyu.im.api.model.dto.ImLoginResultDto;
import com.clmcat.qianyu.im.api.model.dto.ImRefreshDto;
import com.clmcat.qianyu.im.config.ImConfig;
import com.clmcat.qianyu.im.provider.IMProvider;
import com.clmcat.qianyu.im.router.ChannelRouter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * IM 登录业务逻辑
 * 路由到对应厂商 → 确保用户已注册 → 生成 IM Token
 */
@Slf4j
@Service
@DubboService
public class ImLoginServiceBiz implements ImLoginApi {

    @Resource
    private ChannelRouter channelRouter;

    @Resource
    private ImConfig imConfig;

    /**
     * Web 端登录（Controller 调用）
     */
    public ImLoginResultDto login(long userId, ImLoginDto dto) {
        String channel = resolveChannel(dto != null ? dto.getChannel() : null);
        return generateImToken(userId, channel);
    }

    /**
     * Web 端刷新（Controller 调用）
     */
    public ImLoginResultDto refresh(long userId, ImRefreshDto dto) {
        String channel = resolveChannel(dto != null ? dto.getChannel() : null);
        return refreshImToken(userId, channel);
    }

    // ===== RPC 接口实现 =====

    @Override
    public ImLoginResultDto generateImToken(long userId, String channel) {
        channel = resolveChannel(channel);

        log.info("IM 生成登录凭证: userId={}, channel={}", userId, channel);

        IMProvider provider = channelRouter.route(channel);

        // 确保用户在厂商平台已注册
        provider.ensureUserRegistered(userId);

        // 生成 Token
        String imToken = provider.generateToken(userId);

        return ImLoginResultDto.builder()
                .imToken(imToken)
                .channel(channel)
                .sdkAppId(provider.getSdkAppId())
                .build();
    }

    @Override
    public ImLoginResultDto refreshImToken(long userId, String channel) {
        channel = resolveChannel(channel);

        log.info("IM 刷新登录凭证: userId={}, channel={}", userId, channel);

        IMProvider provider = channelRouter.route(channel);
        String imToken = provider.refreshToken(userId);

        return ImLoginResultDto.builder()
                .imToken(imToken)
                .channel(channel)
                .sdkAppId(provider.getSdkAppId())
                .build();
    }

    // ===== 私有方法 =====

    /**
     * 解析渠道，为空则使用默认渠道
     */
    private String resolveChannel(String channel) {
        if (channel != null && !channel.isBlank()) {
            return channel;
        }
        return imConfig.getDefaultChannel();
    }
}
