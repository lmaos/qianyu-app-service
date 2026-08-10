package com.clmcat.qianyu.payment.trade.scheduler;

import com.clmcat.qianyu.payment.trade.mapper.TradeOrderMapper;
import com.clmcat.qianyu.payment.trade.model.entity.TradeOrder;
import com.clmcat.qianyu.payment.trade.service.TradeServiceBiz;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单超时清理定时任务。
 * <p>
 * 每隔 60 秒扫描一次超过 5 分钟仍未确认的 PENDING 订单，自动取消并解冻余额。
 *
 * @author ark-home
 * @date 2026-08-10
 */
@Component
public class OrderTimeoutChecker {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutChecker.class);

    /** 超时阈值（毫秒）：5 分钟 */
    private static final long TIMEOUT_MS = 5 * 60 * 1000;

    /** 每批次处理上限 */
    private static final int BATCH_LIMIT = 100;

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Resource
    private TradeServiceBiz tradeServiceBiz;

    /**
     * 每 60 秒执行一次：扫描超时 PENDING 订单 → 取消解冻。
     */
    @Scheduled(fixedDelay = 60_000)
    public void cleanExpiredOrders() {
        long deadline = System.currentTimeMillis() - TIMEOUT_MS;

        List<TradeOrder> expiredOrders = tradeOrderMapper.customSelectPendingTimeout(deadline, BATCH_LIMIT);
        if (expiredOrders == null || expiredOrders.isEmpty()) {
            return;
        }

        log.info("发现 {} 个超时未确认订单，开始清理", expiredOrders.size());

        int cancelled = 0;
        int failed = 0;

        for (TradeOrder order : expiredOrders) {
            try {
                tradeServiceBiz.cancelOrder(order.getTransNo());
                cancelled++;
                log.debug("超时订单已取消 transNo={}, userId={}, amount={}",
                        order.getTransNo(), order.getFromUserId(), order.getCoinAmount());
            } catch (Exception e) {
                failed++;
                log.error("取消超时订单失败 transNo={}", order.getTransNo(), e);
            }
        }

        log.info("超时订单清理完成: 成功={}, 失败={}", cancelled, failed);
    }
}
