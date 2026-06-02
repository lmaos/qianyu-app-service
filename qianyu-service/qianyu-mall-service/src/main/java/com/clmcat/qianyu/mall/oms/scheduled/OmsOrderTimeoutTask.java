package com.clmcat.qianyu.mall.oms.scheduled;

import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.inv.model.dto.InvStockDto;
import com.clmcat.qianyu.mall.oms.mapper.OmsOrderMapper;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrder;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrderItem;
import com.clmcat.qianyu.mall.oms.rpc.OmsOrderApiImpl;
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

    private static final long TIMEOUT_MS = Long.parseLong(
            System.getProperty("order.timeout.minutes", "30")) * 60_000L;

    @Resource
    private OmsOrderMapper orderMapper;

    @Resource
    private OmsOrderApiImpl orderServiceBiz;

    @Resource
    private InvStockApi invStockApi;

    @Scheduled(fixedRate = 30_000)
    public void cancelTimeoutOrders() {
        long threshold = System.currentTimeMillis() - TIMEOUT_MS;
        List<OmsOrder> expired = orderMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status = ?", OmsOrder.STATUS_PENDING_PAY)
                        .and("create_time < ?", threshold)
                        .and("deleted = 0")
        );
        if (expired.isEmpty()) return;

        log.info("订单超时自动取消: 发现 {} 笔超时未支付订单", expired.size());
        for (OmsOrder order : expired) {
            try {
                order.setStatus(OmsOrder.STATUS_CANCELLED);
                order.setCloseTime(System.currentTimeMillis());
                order.setUpdateTime(System.currentTimeMillis());
                orderMapper.update(order);

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
