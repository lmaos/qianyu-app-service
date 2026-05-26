package com.clmcat.qianyu.storage.router;

import com.clmcat.qianyu.storage.provider.StorageProvider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存储平台路由器
 * 读取 platform 字段，路由到对应的 StorageProvider 实现
 */
@Slf4j
@Service
public class StorageRouter {

    @Resource
    private List<StorageProvider> providers;

    private final Map<String, StorageProvider> providerMap = new HashMap<>();

    @PostConstruct
    void init() {
        for (StorageProvider provider : providers) {
            String platform = provider.getPlatform();
            providerMap.put(platform, provider);
            log.info("存储平台注册: {} -> {}", platform, provider.getClass().getSimpleName());
        }
        log.info("存储平台路由器初始化完成，共注册 {} 个平台", providerMap.size());
    }

    /**
     * 根据 platform 路由到对应的 Provider
     *
     * @param platform 平台标识: aliyun / tencent
     * @return 对应的 StorageProvider 实现
     * @throws RuntimeException 不支持的平台时抛出
     */
    public StorageProvider route(String platform) {
        if (platform == null || platform.isBlank()) {
            throw new RuntimeException("存储平台不能为空");
        }
        StorageProvider provider = providerMap.get(platform);
        if (provider == null) {
            throw new RuntimeException("不支持的存储平台: " + platform + "，支持: " + providerMap.keySet());
        }
        return provider;
    }

    /**
     * 获取默认的 Provider（第一个注册的）
     *
     * @return 默认 StorageProvider
     */
    public StorageProvider getDefault() {
        if (providerMap.isEmpty()) {
            throw new RuntimeException("没有可用的存储平台");
        }
        return providerMap.values().iterator().next();
    }
}
