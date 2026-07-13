package com.clmcat.qianyu.mall.log.tracker;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * M5: Mock 物流轨迹（tracker=nil 时使用）。返回假数据，不调用外部 API。
 */
@Component
public class MockTracker implements LogisticsTracker {

    @Override
    public List<TracePoint> track(String logisticsCode, String logisticsNo) {
        List<TracePoint> points = new ArrayList<>();
        long now = System.currentTimeMillis();
        points.add(new TracePoint(
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(now)),
                "【签收】", "包裹已签收，签收人：本人"));
        points.add(new TracePoint(
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(now - 86400000L)),
                "【派送中】", "快件正在派送中，派送员：张师傅 13800138000"));
        points.add(new TracePoint(
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(now - 2 * 86400000L)),
                "【到达】", "快件已到达目的地城市"));
        points.add(new TracePoint(
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(now - 3 * 86400000L)),
                "【运输中】", "快件离开始发地"));
        return points;
    }

    @Override
    public String providerName() { return "Mock(假数据)"; }
}
