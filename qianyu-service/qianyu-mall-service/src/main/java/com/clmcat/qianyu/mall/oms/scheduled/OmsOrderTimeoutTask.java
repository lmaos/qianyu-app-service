package com.clmcat.qianyu.mall.oms.scheduled;

import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.inv.model.dto.InvStockDto;
import com.clmcat.qianyu.mall.oms.mapper.OmsOrderMapper;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrder;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrderItem;
import com.clmcat.qianyu.mall.oms.rpc.OmsOrderApiImpl;
import com.clmcat.qianyu.mall.pay.config.PayConfig;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OmsOrderTimeoutTask {

    @Resource
    private OmsOrderMapper orderMapper;

    @Resource
    private OmsOrderApiImpl orderServiceBiz;

    @Resource
    private InvStockApi invStockApi;

    @Resource
    private PayConfig payConfig;

    /**
     * 每 30 秒扫描一次，取消超时未支付的订单（status=10 → 50），释放库存。
     * 超时时间通过 application-mall.yml 的 qianyu.mall.pay.timeout.minutes 配置。
     */
    @Scheduled(fixedRate = 30_000)
    public void cancelTimeoutOrders() {
        long timeoutMs = payConfig.getTimeout().getMinutes() * 60_000L;
        long threshold = System.currentTimeMillis() - timeoutMs;
        List<OmsOrder> expired = orderMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status = ?", OmsOrder.STATUS_PENDING_PAY)
                        .and("create_time < ?", threshold)
                        .and("deleted = 0")
        );
        if (expired.isEmpty()) return;

        log.info("订单超时自动取消: 发现 {} 笔超时未支付订单（超时 {} 分钟）", expired.size(), payConfig.getTimeout().getMinutes());
        for (OmsOrder order : expired) {
            try {
                // CAS 取消（防并发：用户正在支付时 status≠10，CAS 失败跳过）
                boolean cancelled = orderServiceBiz.transitStatus(
                        order.getId(), OmsOrder.STATUS_PENDING_PAY, OmsOrder.STATUS_CANCELLED);
                if (!cancelled) {
                    log.info("超时取消跳过(状态已变更): orderId={}", order.getId());
                    continue;
                }

                // 设置关单时间（非关键，单独更新）
                OmsOrder closeUpdate = new OmsOrder();
                closeUpdate.setId(order.getId());
                closeUpdate.setCloseTime(System.currentTimeMillis());
                orderMapper.update(closeUpdate);

                // 释放库存
                List<OmsOrderItem> items = orderServiceBiz.findItemsByOrderId(order.getId());
                if (items != null && !items.isEmpty()) {
                    List<InvStockDto.StockLockItem> releaseItems = new ArrayList<>();
                    for (OmsOrderItem oi : items) {
                        InvStockDto.StockLockItem item = new InvStockDto.StockLockItem();
                        item.setSkuId(oi.getSkuId());
                        item.setQuantity(oi.getQuantity());
                        releaseItems.add(item);
                    }
                    try {
                        invStockApi.releaseStock(order.getOrderNo(), releaseItems);
                    } catch (Exception e) {
                        log.warn("超时取消-释放库存失败, orderId={}, error={}", order.getId(), e.getMessage());
                    }
                }
                log.info("超时取消成功: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
            } catch (Exception e) {
                log.error("超时取消失败: orderId={}, error={}", order.getId(), e.getMessage());
            }
        }
    }
}
