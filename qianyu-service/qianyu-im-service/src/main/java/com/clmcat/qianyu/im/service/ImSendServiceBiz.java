package com.clmcat.qianyu.im.service;

import com.clmcat.qianyu.im.model.body.MessageBody;
import com.clmcat.qianyu.im.model.body.SendRequest;
import com.clmcat.qianyu.im.provider.IMProvider;
import com.clmcat.qianyu.im.router.ChannelRouter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * IM 消息发送业务逻辑
 * 校验 content → 路由 → 调 Provider 发送
 */
@Slf4j
@Service
public class ImSendServiceBiz {

    @Resource
    private ChannelRouter channelRouter;

    /**
     * 发送 IM 消息
     *
     * @param request 发送请求（含 channel + body）
     * @param userId  当前登录用户 ID（用于校验 sender）
     * @return 发送结果描述
     */
    public String send(SendRequest request, long userId) {
        // 1. 参数校验
        MessageBody body = request.getBody();
        if (body == null) {
            throw new RuntimeException("消息体不能为空");
        }
        if (body.getContent() == null || body.getContent().isBlank()) {
            throw new RuntimeException("消息内容不能为空");
        }
        if (body.getSender() == null || body.getSender().isBlank()) {
            throw new RuntimeException("发送者不能为空");
        }
        if (body.getReceiver() == null || body.getReceiver().isBlank()) {
            throw new RuntimeException("接收者不能为空");
        }

        // 2. 校验 sender 与当前用户一致（防止伪造）
        // 当 userId > 0（有 @LoginVerify token）时才校验
        if (userId > 0 && !body.getSender().equals(String.valueOf(userId))) {
            log.warn("IM 发送者校验失败: sender={}, userId={}", body.getSender(), userId);
            throw new RuntimeException("发送者与当前用户不一致");
        }

        // 3. TODO: 内容审核（当前仅校验非空，后续接入审核服务）

        // 4. 路由到对应厂商
        IMProvider provider = channelRouter.route(request.getChannel());

        // 5. 调用 Provider 发送
        log.info("IM 发送: channel={}, sender={}, receiver={}, type={}",
                request.getChannel(), body.getSender(), body.getReceiver(), body.getMessageType());
        String result = provider.sendMessage(body);

        return "OK";
    }
}
