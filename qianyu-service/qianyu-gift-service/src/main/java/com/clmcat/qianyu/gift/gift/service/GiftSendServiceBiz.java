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
 * 送礼核心服务。
 * <p>
 * 流程：校验 → createOrder(冻结余额) → INSERT gift_send_record(SUCCESS) → confirmOrder(扣款+结算)。
 * <p>
 * 冻结即视为已付费：createOrder 成功后余额已扣（移至 frozen），礼物记录直接写 SUCCESS。
 * confirmOrder 完成真实扣款+主播结算，失败由对账重试或取消退款。
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

        // ① 幂等检查
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

        // ⑧ 构建结算子项
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

        // ⑨ 创建订单（冻结余额）。
        // createOrder 已通过 idempotentKey 防重，PENDING 正常；SUCCESS=重试场景；CANCELLED 则拒绝。
        TradeResult createResult = tradeApi.createOrder(tradeReq);
        if (createResult.getStatus() != null && createResult.getStatus() == 2) {
            ResponseStatus.R_OPERATION_FAIL.assertThrowResEx(true, "订单已取消，请更换幂等键重试");
        }

        // ⑩ INSERT 送礼记录（冻结即视为付费成功，直接写 SUCCESS）
        long recordId = GIFT_ID_SNOWFLAKE.nextId();
        String tableName = MonthTableRouter.tableName(TABLE_PREFIX, now);

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
                .status(GiftSendRecord.STATUS_SUCCESS)
                .remark("")
                .createTime(now)
                .build();

        try {
            giftSendRecordMapper.customInsert(tableName, record);
        } catch (DuplicateKeyException e) {
            GiftSendRecord dup = selectByIdempotentKey(key);
            if (dup != null) {
                return toResult(dup);
            }
            throw e;
        }

        // ⑪ 礼物记录已落库，此时即可触发消息。消息发送失败不影响结算。
        try {
            handler.afterSend(record, gift, req);
        } catch (Exception e) {
            log.error("afterSend 失败 recordId={}, 结算继续", recordId, e);
        }

        // ⑫ 确认订单（扣款确认 + 主播结算）。
        // 失败时不调用 cancel——confirm 和 cancel 都是终态，由对账任务根据实际情况决定。
        try {
            tradeApi.confirmOrder(transNo);
        } catch (Exception e) {
            log.error("确认订单失败 transNo={}, 保留 PENDING 等对账处理", transNo, e);
            throw e;
        }

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

    private GiftSendRecord selectByIdempotentKey(String key) {
        String current = MonthTableRouter.currentMonth();
        String tbl = MonthTableRouter.tableName(TABLE_PREFIX, current);
        GiftSendRecord record = giftSendRecordMapper.customSelectByIdempotentKey(tbl, key);
        if (record != null) return record;
        String last = MonthTableRouter.queryMonths(current, 2).get(1);
        try {
            return giftSendRecordMapper.customSelectByIdempotentKey(
                    MonthTableRouter.tableName(TABLE_PREFIX, last), key);
        } catch (Exception e) {
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
