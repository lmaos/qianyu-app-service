package com.clmcat.qianyu.mall.log.tracker;

import java.util.List;

/**
 * M5: 物流轨迹查询策略接口。
 * <p>实现：{@link MockTracker}(假数据) / {@link KdniaoTracker}(快递鸟) / {@link Kuaidi100Tracker}(快递100)。
 * <p>由 {@link LogisticsTrackerFactory} 根据 {@code qianyu.mall.logistics.tracker} 配置选择实现。
 */
public interface LogisticsTracker {

    /**
     * 查询物流轨迹。
     *
     * @param logisticsCode 物流公司编码（如 SF / YTO / ZTO）
     * @param logisticsNo   物流单号
     * @return 轨迹点列表（按时间倒序，空列表表示无轨迹）
     */
    List<TracePoint> track(String logisticsCode, String logisticsNo);

    /** 提供商名称（用于日志/展示） */
    String providerName();

    /** 轨迹点 */
    class TracePoint {
        private final String time;
        private final String location;
        private final String description;

        public TracePoint(String time, String location, String description) {
            this.time = time;
            this.location = location;
            this.description = description;
        }
        public String getTime() { return time; }
        public String getLocation() { return location; }
        public String getDescription() { return description; }
    }
}
