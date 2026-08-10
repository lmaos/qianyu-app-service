package com.clmcat.qianyu.payment.trade.service;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.core.table.MonthTableRouter;
import com.clmcat.qianyu.payment.api.trade.TradeApi;
import com.clmcat.qianyu.payment.api.trade.model.dto.SettleItem;
import com.clmcat.qianyu.payment.api.trade.model.dto.TradeRequest;
import com.clmcat.qianyu.payment.api.trade.model.dto.TradeResult;
import com.clmcat.qianyu.payment.settlement.model.entity.SettlementRecord;
import com.clmcat.qianyu.payment.trade.mapper.TradeOrderItemMapper;
import com.clmcat.qianyu.payment.trade.mapper.TradeOrderMapper;
import com.clmcat.qianyu.payment.trade.model.entity.TradeOrder;
import com.clmcat.qianyu.payment.trade.model.entity.TradeOrderItem;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易编排服务（下单模式 + 母子表）。
 * <p>
 * 两阶段提交：
 * <ol>
 *   <li><b>createOrder</b>：冻结余额 + INSERT trade_order（母表） + INSERT trade_order_item × N（子表）</li>
 *   <li><b>confirmOrder</b>：确认冻结（创建扣款流水）+ 遍历子项逐条结算 + UPDATE trade_order (SUCCESS)</li>
 * </ol>
 * 超时未确认的订单由 {@link com.clmcat.qianyu.payment.trade.scheduler.OrderTimeoutChecker} 自动取消解冻。
 * <p>
 * 模块内部通过 {@code @Resource} 直接注入，保证 Spring {@code @Transactional} 事务传播。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@DubboService
@Service
public class TradeServiceBiz implements TradeApi {

    private static final CustomSnowflake ORDER_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    private static final String ITEM_TABLE_PREFIX = "trade_order_item";

    @Resource
    private com.clmcat.qianyu.payment.wallet.service.WalletServiceBiz walletServiceBiz;

    @Resource
    private com.clmcat.qianyu.payment.settlement.service.SettlementServiceBiz settlementServiceBiz;

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;

    // ==================== ① 创建订单（冻结余额） ====================

    @Override
    @Transactional
    public TradeResult createOrder(TradeRequest req) {
        validateRequest(req);

        String transNo = req.getTransNo().trim();
        String key = req.getIdempotentKey().trim();
        List<SettleItem> items = req.getItems();

        // 幂等检查
        TradeOrder existing = tradeOrderMapper.customSelectByIdempotentKey(key);
        if (existing != null) {
            return TradeResult.builder()
                    .tradeId(existing.getId())
                    .transNo(existing.getTransNo())
                    .status(existing.getStatus())
                    .build();
        }

        long now = System.currentTimeMillis();
        String bizType = req.getBizType().trim();
        String bizId = req.getBizId() != null ? req.getBizId().trim() : "";

        // ① 冻结余额（一次冻结，总额），不在此时创建交易流水
        walletServiceBiz.freeze(req.getFromUserId(), req.getCoinAmount());

        // ② 创建交易订单母表（PENDING）
        long orderId = ORDER_ID_SNOWFLAKE.nextId();
        TradeOrder order = TradeOrder.builder()
                .id(orderId)
                .transNo(transNo)
                .fromUserId(req.getFromUserId())
                .coinAmount(req.getCoinAmount())
                .bizType(bizType)
                .bizId(bizId)
                .idempotentKey(key)
                .status(TradeOrder.STATUS_PENDING)
                .createTime(now)
                .build();

        try {
            tradeOrderMapper.customInsert(order);
        } catch (DuplicateKeyException e) {
            TradeOrder dup = tradeOrderMapper.customSelectByIdempotentKey(key);
            if (dup != null) {
                return TradeResult.builder().tradeId(dup.getId()).transNo(dup.getTransNo()).status(dup.getStatus()).build();
            }
            throw e;
        }

        // ③ 批量插入结算子项（路由到 UTC 月表）
        String itemTable = MonthTableRouter.tableName(ITEM_TABLE_PREFIX, now);
        List<TradeOrderItem> itemEntities = new ArrayList<>(items.size());
        for (SettleItem si : items) {
            itemEntities.add(TradeOrderItem.builder()
                    .id(ORDER_ID_SNOWFLAKE.nextId())
                    .orderId(orderId)
                    .transNo(transNo)
                    .bizNo(si.getBizNo().trim())
                    .fromUserId(req.getFromUserId())
                    .toUserId(si.getToUserId())
                    .settleAmount(si.getSettleAmount())
                    .commissionRate(si.getCommissionRate())
                    .status(TradeOrderItem.STATUS_PENDING)
                    .createTime(now)
                    .build());
        }
        tradeOrderItemMapper.customBatchInsert(itemTable, orderId, itemEntities);

        return TradeResult.builder()
                .tradeId(orderId)
                .transNo(transNo)
                .status(TradeOrder.STATUS_PENDING)
                .build();
    }

    // ==================== ② 确认订单（扣款+结算） ====================

    @Override
    @Transactional
    public TradeResult confirmOrder(String transNo) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(transNo == null || transNo.trim().isEmpty(), "交易流水号不能为空");
        String tn = transNo.trim();

        TradeOrder order = tradeOrderMapper.customSelectByTransNo(tn);
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx(order == null, "交易订单不存在: " + tn);

        // 已确认 → 幂等返回
        if (order.getStatus() == TradeOrder.STATUS_SUCCESS) {
            return TradeResult.builder()
                    .tradeId(order.getId())
                    .transNo(order.getTransNo())
                    .status(order.getStatus())
                    .build();
        }

        // 非 PENDING 状态不可确认
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(order.getStatus() != TradeOrder.STATUS_PENDING,
                "订单状态不允许确认，当前状态=" + order.getStatus());

        // ① 确认冻结：创建扣款流水 + frozen_balance -= amount
        walletServiceBiz.confirmFreeze(order.getFromUserId(), order.getCoinAmount(),
                order.getBizType(), order.getTransNo(), order.getIdempotentKey() + "_coin");

        // ② 加载子项（从对应月表），逐条结算
        String itemTable = MonthTableRouter.tableName(ITEM_TABLE_PREFIX, order.getCreateTime());
        List<TradeOrderItem> items = tradeOrderItemMapper.customSelectByOrderId(itemTable, order.getId());
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx(items == null || items.isEmpty(), "交易订单无结算明细: " + tn);

        for (TradeOrderItem item : items) {
            // 每条结算使用独立幂等键：key + "_settle_" + bizNo
            String settleKey = order.getIdempotentKey() + "_settle_" + item.getBizNo();
            settlementServiceBiz.credit(item.getToUserId(), item.getSettleAmount(),
                    SettlementRecord.TYPE_GIFT, order.getTransNo(), item.getBizNo(),
                    item.getCommissionRate(), settleKey);

            // 更新子项状态 PENDING → SUCCESS
            tradeOrderItemMapper.customUpdateStatus(itemTable, item.getId(),
                    TradeOrderItem.STATUS_PENDING, TradeOrderItem.STATUS_SUCCESS);
        }

        // ③ 更新母订单状态：PENDING → SUCCESS（乐观锁，并发重试时可能已被另一个线程确认）
        int updated = tradeOrderMapper.customUpdateStatus(order.getId(),
                TradeOrder.STATUS_PENDING, TradeOrder.STATUS_SUCCESS);
        if (updated == 0) {
            // 可能是并发重试 → 幂等返回已有结果
            TradeOrder latest = tradeOrderMapper.customSelectByTransNo(tn);
            if (latest != null && latest.getStatus() == TradeOrder.STATUS_SUCCESS) {
                return TradeResult.builder()
                        .tradeId(latest.getId())
                        .transNo(latest.getTransNo())
                        .status(latest.getStatus())
                        .build();
            }
            ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(true, "订单状态已变更，确认失败: " + tn);
        }

        return TradeResult.builder()
                .tradeId(order.getId())
                .transNo(order.getTransNo())
                .status(TradeOrder.STATUS_SUCCESS)
                .build();
    }

    // ==================== ③ 查询订单 ====================

    @Override
    public TradeResult getOrder(String transNo) {
        if (transNo == null || transNo.trim().isEmpty()) {
            return null;
        }
        TradeOrder order = tradeOrderMapper.customSelectByTransNo(transNo.trim());
        if (order == null) {
            return null;
        }
        return TradeResult.builder()
                .tradeId(order.getId())
                .transNo(order.getTransNo())
                .status(order.getStatus())
                .build();
    }

    // ==================== ④ 取消订单（解冻余额） ====================

    @Override
    @Transactional
    public void cancelOrder(String transNo) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(transNo == null || transNo.trim().isEmpty(), "交易流水号不能为空");
        String tn = transNo.trim();

        TradeOrder order = tradeOrderMapper.customSelectByTransNo(tn);
        if (order == null) {
            return;
        }

        if (order.getStatus() != TradeOrder.STATUS_PENDING) {
            return;
        }

        // ① 解冻余额
        walletServiceBiz.unfreeze(order.getFromUserId(), order.getCoinAmount());

        // ② 更新母订单 PENDING → CANCELLED
        tradeOrderMapper.customUpdateStatus(order.getId(),
                TradeOrder.STATUS_PENDING, TradeOrder.STATUS_CANCELLED);

        // ③ 更新所有子项 PENDING → CANCELLED
        String itemTable = MonthTableRouter.tableName(ITEM_TABLE_PREFIX, order.getCreateTime());
        List<TradeOrderItem> items = tradeOrderItemMapper.customSelectByOrderId(itemTable, order.getId());
        if (items != null) {
            for (TradeOrderItem item : items) {
                tradeOrderItemMapper.customUpdateStatus(itemTable, item.getId(),
                        TradeOrderItem.STATUS_PENDING, TradeOrderItem.STATUS_CANCELLED);
            }
        }
    }

    // ---- 私有方法 ----

    private void validateRequest(TradeRequest req) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req == null);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getFromUserId() == null || req.getFromUserId() <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getCoinAmount() == null || req.getCoinAmount() <= 0, "消费金额必须大于0");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getBizType() == null || req.getBizType().trim().isEmpty(), "业务类型不能为空");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getTransNo() == null || req.getTransNo().trim().isEmpty(), "交易流水号不能为空");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getIdempotentKey() == null || req.getIdempotentKey().trim().isEmpty(), "幂等键不能为空");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getItems() == null || req.getItems().isEmpty(), "结算明细不能为空");

        long now = System.currentTimeMillis(); // unused but kept for context
        for (SettleItem si : req.getItems()) {
            ResponseStatus.P_VALUE_ERROR.assertThrowResEx(si.getBizNo() == null || si.getBizNo().trim().isEmpty(), "结算明细 bizNo 不能为空");
            ResponseStatus.P_VALUE_ERROR.assertThrowResEx(si.getToUserId() == null || si.getToUserId() <= 0, "结算明细 toUserId 无效");
            ResponseStatus.P_VALUE_ERROR.assertThrowResEx(si.getSettleAmount() == null || si.getSettleAmount() <= 0, "结算明细 settleAmount 必须大于0");
            ResponseStatus.P_VALUE_ERROR.assertThrowResEx(si.getCommissionRate() == null || si.getCommissionRate() < 0 || si.getCommissionRate() > 10000,
                    "结算分佣比例需在 0~10000 之间（万分比）");
        }
    }
}
