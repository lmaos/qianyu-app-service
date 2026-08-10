package com.clmcat.qianyu.live.room.service;

import com.clmcat.qianyu.live.api.counter.LiveRoomCounterApi;
import com.clmcat.qianyu.live.room.mapper.LiveRoomCountMapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * 直播间计数器 RPC 服务。
 * <p>
 * 实现 {@link LiveRoomCounterApi}，供礼物模块等跨模块调用计数器原子操作。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@DubboService
@Service
public class LiveRoomCounterServiceBiz implements LiveRoomCounterApi {

    @Resource
    private LiveRoomCountMapper liveRoomCountMapper;

    @Override
    public boolean incrementGiftCount(long roomId, long count, long amount) {
        int affected = liveRoomCountMapper.customIncrGiftCount(roomId, count, amount);
        return affected > 0;
    }
}
