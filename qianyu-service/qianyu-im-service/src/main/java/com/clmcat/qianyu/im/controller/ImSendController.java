package com.clmcat.qianyu.im.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.im.model.body.SendRequest;
import com.clmcat.qianyu.im.service.ImSendServiceBiz;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * IM 消息发送接口
 * POST /api/im/send
 */
@Slf4j
@ApiController
@RequestMapping("/api/im")
public class ImSendController {

    @Resource
    private ImSendServiceBiz imSendServiceBiz;

    /**
     * 发送 IM 消息
     * 请求体: { "channel": "tencent", "body": { ... } }
     */
    @RequestMapping("/send")
    public String send(@Params SendRequest request, @Token long userId) {
        log.info("收到 IM 发送请求: channel={}, userId={}", request.getChannel(), userId);
        return imSendServiceBiz.send(request, userId);
    }
}
