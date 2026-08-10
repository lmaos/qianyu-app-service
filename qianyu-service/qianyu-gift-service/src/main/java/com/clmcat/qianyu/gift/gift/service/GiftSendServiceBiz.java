package com.clmcat.qianyu.gift.gift.service;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.core.table.MonthTableRouter;
import com.clmcat.qianyu.gift.api.gift.GiftApi;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftDto;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendRequest;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendResult;
import com.clmcat.qianyu.gift.gift.mapper.GiftConfigMapper;
import com.clmcat.qianyu.gift.gift.mapper.GiftSendRecordMapper;
import com.clmcat.qianyu.gift.gift.model.entity.GiftConfig;
import com.clmcat.qianyu.gift.gift.model.entity.GiftSendRecord;
import com.clmcat.qianyu.gift.gift.support.GiftSupport;
import com.clmcat.qianyu.gift.model.entity.status.GiftStatus;
import com.clmcat.qianyu.gift.rule.GiftTypeHandler;
import com.clmcat.qianyu.gift.rule.GiftTypeHandlerRegistry;
import com.clmcat.qianyu.payment.api.trade.TradeApi;
import com.clmcat.qianyu.payment.api.trade.model.dto.SettleItem;
import com.clmcat.qianyu.payment.api.trade.model.dto.TradeRequest;
import com.clmcat.qianyu.payment.api.trade.model.dto.TradeResult;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 送礼核心服务（下单模式 + 月表路由）。
 * <p>
 * 实现 {@link GiftApi}，通过 Dubbo RPC 暴露。
 * 编排送礼全流程：校验 → 创建订单（冻结余额）→ 记录流水（PENDING）→ 确认订单（扣款+结算）→ 标记完成。
 * <p>
 * 流程设计（下单模式）：
 * <ol>
 *   <li>校验参数、加载礼物配置、计算价格和佣金</li>
 *   <li>盲盒开奖（普通礼物跳过）</li>
 *   <li>生成统一交易流水号 {@code transNo}</li>
 *   <li>调用 {@link TradeApi#createOrder} — 冻结余额 + 创建 PENDING 订单</li>
 *   <li>INSERT gift_send_record_{月份}（PENDING）— 按 createTime 路由到 UTC 月表</li>
 *   <li>调用 {@link TradeApi#confirmOrder} — 确认扣款 + 结算收入 + 订单→SUCCESS</li>
 *   <li>UPDATE gift_send_record_{月份} → SUCCESS</li>
 * </ol>
 * <p>
 * 月表路由：查询时本月 + 上月双表搜索（覆盖全球时区跨月）。插入按 createTime 确定月表。
 * <p>
 * 异常处理：
 * <ul>
 *   <li>confirmOrder 失败 → 调用 cancelOrder 解冻余额，标记礼物记录 CANCELLED</li>
 *   <li>超时 PENDING 订单由 {@code OrderTimeoutChecker} 定时清理</li>
 * </ul>
 * <p>
 * 幂等：gift_send_record.uk_idempotent_key 防重，trade_order.uk_idempotent_key 防重。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@DubboService
@Service
public class GiftSendServiceBiz extends GiftSupport implements GiftApi {

    private static final Logger log = LoggerFactory.getLogger(GiftSendServiceBiz.class);

    private static final String TABLE_PREFIX = "gift_send_record";

    @Resource
    private GiftConfigMapper giftConfigMapper;

    @Resource
    private GiftSendRecordMapper giftSendRecordMapper;

    @Resource
    private GiftTypeHandlerRegistry handlerRegistry;

    @DubboReference
    private TradeApi tradeApi;

    @Override
    public GiftSendResult sendGift(GiftSendRequest req) {
        validateRequest(req);

        long now = System.currentTimeMillis();
        String key = req.getIdempotentKey().trim();
        String tableName = MonthTableRouter.tableName(TABLE_PREFIX, now);

        // ① 幂等检查：本月 + 上月
        GiftSendRecord existing = selectByIdempotentKey(key);
        if (existing != null) {
            return toResult(existing);
        }

        // ② 加载礼物配置
        GiftConfig gift = giftConfigMapper.customSelectById(req.getGiftId());
        GiftStatus.GIFT_NOT_FOUND.assertThrowResEx(gift == null);
        GiftStatus.GIFT_DISABLED.assertThrowResEx(gift.getStatus() != GiftConfig.STATUS_ENABLED);
        GiftStatus.SCENE_NOT_SUPPORTED.assertThrowResEx(
                gift.getShelfScenes() == null || !gift.getShelfScenes().contains(req.getSceneType()));

        // ③ 获取类型处理器
        GiftTypeHandler handler = handlerRegistry.getRequiredHandler(gift.getGiftType());
        handler.checkUnlock(req.getSenderUserId(), gift, req);

        // ④ 计算价格和佣金
        int quantity = req.getQuantity() != null && req.getQuantity() > 0 ? req.getQuantity() : 1;
        long totalPrice = handler.resolvePrice(gift, quantity);
        int commissionRate = handler.resolveCommissionRate(gift);

        // ⑤ 盲盒开奖
        GiftTypeHandler.OpenResult openResult = handler.open(gift, req);

        // ⑥ 生成统一交易流水号
        String transNo = allocateTransNo();

        // ⑦ 计算结算金额
        long settleAmount = handler.resolveSettleAmount(gift, openResult, totalPrice, commissionRate);

        // ⑧ 创建订单（冻结余额 + INSERT trade_order PENDING + INSERT trade_order_item × 1）
        SettleItem settleItem = SettleItem.builder()
                .bizNo(transNo)
                .toUserId(req.getReceiverUserId())
                .settleAmount(settleAmount)
                .commissionRate(commissionRate)
                .build();

        TradeRequest tradeReq = TradeRequest.builder()
                .fromUserId(req.getSenderUserId())
                .coinAmount(totalPrice)
                .bizType("gift")
                .bizId(String.valueOf(req.getGiftId()))
                .transNo(transNo)
                .idempotentKey(key)
                .items(Collections.singletonList(settleItem))
                .build();

        TradeResult createResult = tradeApi.createOrder(tradeReq);

        // ⑨ INSERT 送礼记录到月表（PENDING。单次送 bizNo = transNo）
        long recordId = GIFT_ID_SNOWFLAKE.nextId();

        GiftSendRecord record = GiftSendRecord.builder()
                .id(recordId)
                .transNo(transNo)
                .bizNo(transNo)
                .senderUserId(req.getSenderUserId())
                .receiverUserId(req.getReceiverUserId())
                .giftId(gift.getId())
                .giftName(gift.getName())
                .giftPrice(gift.getPrice())
                .quantity(quantity)
                .totalAmount(totalPrice)
                .actualGiftId(openResult != null ? openResult.actualGiftId() : null)
                .actualGiftName(openResult != null ? openResult.actualGiftName() : null)
                .sceneType(req.getSceneType())
                .roomId(req.getRoomId())
                .payType(req.getPayType() != null ? req.getPayType() : GiftSendRecord.PAY_TYPE_COIN)
                .idempotentKey(key)
                .commissionRate(commissionRate)
                .settleAmount(settleAmount)
                .status(GiftSendRecord.STATUS_PENDING)
                .remark("")
                .createTime(now)
                .build();

        try {
            giftSendRecordMapper.customInsert(tableName, record);
        } catch (DuplicateKeyException e) {
            // 并发冲突 → 本月 + 上月再查一次
            GiftSendRecord dup = selectByIdempotentKey(key);
            if (dup != null) {
                return toResult(dup);
            }
            throw e;
        }

        // ⑩ 确认订单（扣款确认 + 结算收入 + trade_order → SUCCESS）
        try {
            tradeApi.confirmOrder(transNo);
        } catch (Exception e) {
            log.error("确认订单失败 transNo={}, 尝试取消订单解冻余额", transNo, e);
            try {
                tradeApi.cancelOrder(transNo);
            } catch (Exception cancelEx) {
                log.error("取消订单也失败 transNo={}, 需人工处理或等定时清理", transNo, cancelEx);
            }
            throw e;
        }

        // ⑪ 标记送礼记录为 SUCCESS（与 INSERT 同表）
        int updated = giftSendRecordMapper.customUpdateStatus(tableName, recordId,
                GiftSendRecord.STATUS_PENDING, GiftSendRecord.STATUS_SUCCESS);
        if (updated == 0) {
            log.warn("更新送礼记录状态失败 recordId={}, 可能已被并发修改", recordId);
        }

        record.setStatus(GiftSendRecord.STATUS_SUCCESS);

        // ⑫ 后置处理（任务进度、奖池入池等，Phase 2 实现）
        handler.afterSend(record, gift, req);

        // TODO: 送礼后推送 WebSocket 消息到直播间，触发礼物动画播放

        return toResult(record);
    }

    @Override
    public GiftDto getGift(long giftId) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(giftId <= 0);
        GiftConfig gift = giftConfigMapper.customSelectById(giftId);
        return toDto(gift);
    }

    @Override
    public List<GiftDto> batchGetGifts(List<Long> giftIds) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(giftIds == null || giftIds.isEmpty());
        List<GiftConfig> gifts = giftConfigMapper.customSelectByIds(giftIds);
        return gifts.stream().map(GiftSupport::toDto).collect(Collectors.toList());
    }

    // ---- 私有方法 ----

    /**
     * 按幂等键查询，本月 → 上月。
     * <p>
     * 覆盖全球时区 ±14h 边界：UTC 本月初的请求在东 12 区可能落在上月。
     * 上月表不存在时忽略（启动初期或首月部署）。
     */
    private GiftSendRecord selectByIdempotentKey(String key) {
        String current = MonthTableRouter.currentMonth();
        String currentTable = MonthTableRouter.tableName(TABLE_PREFIX, current);
        GiftSendRecord record = giftSendRecordMapper.customSelectByIdempotentKey(currentTable, key);
        if (record != null) {
            return record;
        }
        // 上月兜底
        String lastMonth = MonthTableRouter.queryMonths(current, 2).get(1);
        String lastTable = MonthTableRouter.tableName(TABLE_PREFIX, lastMonth);
        try {
            return giftSendRecordMapper.customSelectByIdempotentKey(lastTable, key);
        } catch (Exception e) {
            log.debug("上月表查询失败（可能表不存在）: table={}", lastTable);
            return null;
        }
    }

    private void validateRequest(GiftSendRequest req) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req == null);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getSenderUserId() == null || req.getSenderUserId() <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getReceiverUserId() == null || req.getReceiverUserId() <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getGiftId() == null || req.getGiftId() <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getSceneType() == null || req.getSceneType().trim().isEmpty(),
                "场景类型不能为空");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(req.getIdempotentKey() == null || req.getIdempotentKey().trim().isEmpty(),
                "幂等键不能为空");
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(
                req.getSenderUserId().equals(req.getReceiverUserId()), "不能给自己送礼");
    }
}
