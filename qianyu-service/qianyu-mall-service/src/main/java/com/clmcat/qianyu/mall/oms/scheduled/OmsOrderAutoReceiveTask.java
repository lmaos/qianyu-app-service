package com.clmcat.qianyu.mall.oms.scheduled;

import com.clmcat.qianyu.mall.oms.config.OmsConfig;
import com.clmcat.qianyu.mall.oms.mapper.OmsOrderMapper;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrder;
import com.clmcat.qianyu.mall.oms.rpc.OmsOrderApiImpl;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * S22: 发货后超时自动确认收货任务。
 * <p>扫描待收货(status=30)且超过 {@code auto-receive.days} 未确认的订单，CAS 推进 30→40 + receive_time。
 * <p>仿 {@link OmsOrderTimeoutTask} / PmsSpuAuditTask 模式。必须走 CAS（markReceived）防与买家手动确认并发双推进。
 */
@Slf4j
@Component
public class OmsOrderAutoReceiveTask {

    @Resource
    private OmsOrderMapper orderMapper;

    @Resource
    private OmsOrderApiImpl orderServiceBiz;

    @Resource
    private OmsConfig omsConfig;

    /** 系统通知投递（降级，不阻断主流程——决策 D-05）。 */
    @DubboReference
    private com.clmcat.qianyu.mall.api.msg.MsgApi msgApi;

    private void notifySafely(Long userId, Integer type, String title, String content, String bizType, Long bizId) {
        if (userId == null || userId <= 0) return;
        try { msgApi.send(userId, type, title, content, bizType, bizId); }
        catch (Exception e) { log.warn("通知投递失败 type={} bizId={}: {}", type, bizId, e.getMessage()); }
    }

    @Scheduled(fixedRate = 60_000)
    public void autoReceive() {
        long timeoutMs = omsConfig.getAutoReceive().getDays() * 86400_000L;
        long threshold = System.currentTimeMillis() - timeoutMs;
        List<OmsOrder> pending = orderMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status = ?", OmsOrder.STATUS_SHIPPED)
                        .and("delivery_time < ?", threshold)
                        .and("deleted = 0"));
        if (pending == null || pending.isEmpty()) return;

        log.info("自动确认收货: 发现 {} 笔超时待收货订单（{} 天）", pending.size(), omsConfig.getAutoReceive().getDays());
        int received = 0;
        for (OmsOrder order : pending) {
            try {
                // CAS 30→40（markReceived WHERE status+version），防与手动确认并发
                if (orderServiceBiz.markReceived(order.getId())) {
                    received++;
                    notifySafely(order.getUserId(), 2, "订单已自动确认收货", "超时未操作，系统已自动确认收货。", "auto_received", order.getId());
                }
            } catch (Exception e) {
                log.error("自动确认收货失败 orderId={} error={}", order.getId(), e.getMessage());
            }
        }
        log.info("自动确认收货: 本次确认 {} 笔", received);
    }
}
