package com.clmcat.qianyu.im.router;

import com.clmcat.qianyu.im.provider.IMProvider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 渠道路由器
 * 读取 channel 字段，路由到对应的 IMProvider 实现
 */
@Slf4j
@Service
public class ChannelRouter {

    @Resource
    private List<IMProvider> providers;

    private final Map<String, IMProvider> providerMap = new HashMap<>();

    @PostConstruct
    void init() {
        for (IMProvider provider : providers) {
            String channel = provider.getChannel();
            providerMap.put(channel, provider);
            log.info("IM 渠道注册: {} -> {}", channel, provider.getClass().getSimpleName());
        }
        log.info("IM 渠道路由器初始化完成，共注册 {} 个渠道", providerMap.size());
    }

    /**
     * 根据 channel 路由到对应的 Provider
     *
     * @param channel 渠道标识: tencent / easemob / rongcloud / nim
     * @return 对应的 IMProvider 实现
     * @throws RuntimeException 不支持的渠道时抛出
     */
    public IMProvider route(String channel) {
        if (channel == null || channel.isBlank()) {
            throw new RuntimeException("IM 渠道不能为空");
        }
        IMProvider provider = providerMap.get(channel);
        if (provider == null) {
            throw new RuntimeException("不支持的 IM 渠道: " + channel + "，支持: " + providerMap.keySet());
        }
        return provider;
    }

    /**
     * 获取所有已注册的渠道
     */
    public Map<String, IMProvider> getProviderMap() {
        return Map.copyOf(providerMap);
    }
}
