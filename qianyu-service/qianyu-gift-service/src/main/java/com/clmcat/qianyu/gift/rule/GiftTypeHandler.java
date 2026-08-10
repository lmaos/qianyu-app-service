package com.clmcat.qianyu.gift.rule;

import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendRequest;
import com.clmcat.qianyu.gift.gift.model.entity.GiftConfig;
import com.clmcat.qianyu.gift.gift.model.entity.GiftSendRecord;

/**
 * 礼物类型处理器。
 * <p>
 * 每种礼物类型对应一个实现，通过 {@link GiftTypeHandlerRegistry} 注册。
 * 新增礼物类型只需添加新的 Handler 实现，无需修改送礼主流程。
 *
 * @author ark-home
 * @date 2026-08-07
 */
public interface GiftTypeHandler {

    /** 匹配的礼物类型 */
    int giftType();

    /**
     * 发送前校验（锁定/条件/专属/活动等）。
     * 不满足条件时抛出业务异常。
     *
     * @param senderUserId 送礼人用户ID
     * @param gift         礼物配置
     * @param req          送礼请求
     */
    default void checkUnlock(long senderUserId, GiftConfig gift, GiftSendRequest req) {
    }

    /**
     * 解析实际应付金额。
     * <ul>
     *   <li>普通礼物：price × quantity</li>
     *   <li>盲盒/奖池：price（quantity 恒为 1）</li>
     * </ul>
     */
    long resolvePrice(GiftConfig gift, int quantity);

    /**
     * 解析分佣比例。默认返回礼物配置的比例。
     */
    default int resolveCommissionRate(GiftConfig gift) {
        return gift.getCommissionRate() != null ? gift.getCommissionRate() : 5000;
    }

    /**
     * 开奖（盲盒随机开出实际礼物）。
     *
     * @param gift 礼物配置
     * @param req  送礼请求
     * @return 开奖结果，null 表示无开出结果（普通礼物）
     */
    default OpenResult open(GiftConfig gift, GiftSendRequest req) {
        return null;
    }

    /**
     * 计算结算金额。
     *
     * @param gift      原始礼物配置
     * @param opened    开奖结果（可为 null）
     * @param totalPaid 实际支付金额
     * @param rate      分佣比例（万分比）
     * @return 主播应得结算金额
     */
    default long resolveSettleAmount(GiftConfig gift, OpenResult opened, long totalPaid, int rate) {
        // 默认：结算金额 = 支付金额 × (10000 - 佣金比例) / 10000
        return totalPaid * (10000 - rate) / 10000;
    }

    /**
     * 发送后置处理（图鉴进度、任务进度、奖池入池等）。
     * 在送礼记录已持久化后调用，同一事务内。
     *
     * @param record 送礼记录
     * @param gift   礼物配置
     * @param req    送礼请求
     */
    default void afterSend(GiftSendRecord record, GiftConfig gift, GiftSendRequest req) {
    }

    /**
     * 开奖结果。
     */
    record OpenResult(long actualGiftId, String actualGiftName, long actualPrice) {
    }
}
