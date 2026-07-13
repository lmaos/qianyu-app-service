package com.clmcat.qianyu.mall.log.tracker;

import com.clmcat.qianyu.mall.log.config.LogisticsConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * M5: 物流轨迹策略工厂。根据 {@code qianyu.mall.logistics.tracker} 配置选择实现。
 * <ul>
 *   <li>{@code nil} → {@link MockTracker}（假数据，不调外部 API）</li>
 *   <li>{@code kdniao} → {@link KdniaoTracker}（快递鸟即时查询）</li>
 *   <li>{@code kuaidi100} → {@link Kuaidi100Tracker}（快递100实时查询）</li>
 * </ul>
 */
@Slf4j
@Component
public class LogisticsTrackerFactory {

    @Resource
    private LogisticsConfig config;

    @Resource
    private MockTracker mockTracker;

    private LogisticsTracker activeTracker;

    @PostConstruct
    public void init() {
        String tracker = config.getTracker();
        if (tracker == null || "nil".equalsIgnoreCase(tracker) || tracker.isEmpty()) {
            activeTracker = mockTracker;
            log.info("物流轨迹：使用 Mock 假数据（tracker=nil）");
        } else if ("kdniao".equalsIgnoreCase(tracker)) {
            activeTracker = new KdniaoTracker(config.getKdniao());
            log.info("物流轨迹：使用 快递鸟(kdniao) businessId={}", config.getKdniao().getBusinessId());
        } else if ("kuaidi100".equalsIgnoreCase(tracker)) {
            activeTracker = new Kuaidi100Tracker(config.getKuaidi100());
            log.info("物流轨迹：使用 快递100(kuaidi100) customer={}", config.getKuaidi100().getCustomer());
        } else {
            activeTracker = mockTracker;
            log.warn("物流轨迹：未知 tracker={}，回退 Mock", tracker);
        }
    }

    public LogisticsTracker getTracker() {
        return activeTracker;
    }
}
