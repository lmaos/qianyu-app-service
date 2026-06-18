package com.clmcat.qianyu.social.visitor.service;

import com.clmcat.qianyu.social.api.visitor.VisitorApi;
import com.clmcat.qianyu.social.api.visitor.model.dto.VisitorCountDto;
import com.clmcat.qianyu.social.visitor.model.entity.status.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * 访客统计服务。
 * <p>
 * TODO: 后续接入埋点系统，当前返回 0。
 */
@Slf4j
@Service
@DubboService
public class VisitorServiceBiz implements VisitorApi {

    @Override
    public VisitorCountDto getVisitorCount(long userId) {
        Status.USER_REQUIRED.assertThrowResEx(userId <= 0);
        // TODO: 后续接入埋点系统，从 Redis / DB 读取真实新访客数
        log.debug("getVisitorCount: userId={}, returning 0 (TODO: not implemented)", userId);
        return VisitorCountDto.builder()
                .userId(userId)
                .visitorCount(0L)
                .build();
    }
}
