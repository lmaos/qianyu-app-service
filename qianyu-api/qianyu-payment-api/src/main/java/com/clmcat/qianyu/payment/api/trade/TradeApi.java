package com.clmcat.qianyu.payment.api.trade;

import com.clmcat.qianyu.payment.api.trade.model.dto.TradeRequest;
import com.clmcat.qianyu.payment.api.trade.model.dto.TradeResult;

/**
 * 交易编排 RPC API（下单模式）。
 * <p>
 * 两阶段提交：① createOrder 冻结余额 → ② confirmOrder 确认扣款+结算。
 * 超时未确认的订单由定时任务自动 cancelOrder 解冻。
 *
 * @author ark-home
 * @date 2026-08-03
 */
public interface TradeApi {

    /**
     * ① 创建订单：冻结余额 + INSERT trade_order（status=PENDING）。
     * <p>
     * 幂等：重复的 {@code idempotentKey} 返回已有订单，不重复冻结。
     *
     * @param req 交易请求（含 transNo、idempotentKey）
     * @return 交易结果（status=PENDING）
     */
    TradeResult createOrder(TradeRequest req);

    /**
     * ② 确认订单：扣款确认（frozen→spent）+ 结算收入 + UPDATE trade_order（status=SUCCESS）。
     * <p>
     * 幂等：订单已 SUCCESS 则直接返回。
     *
     * @param transNo 统一交易流水号
     * @return 交易结果（status=SUCCESS）
     */
    TradeResult confirmOrder(String transNo);

    /**
     * 取消订单：解冻余额 + UPDATE trade_order（status=CANCELLED）。
     * <p>
     * 仅 PENDING 状态的订单可取消。
     *
     * @param transNo 统一交易流水号
     */
    void cancelOrder(String transNo);

    /**
     * 查询订单状态。
     * <p>
     * 用于对账：确认阶段超时后，调用方可通过此接口获知服务端真实状态。
     *
     * @param transNo 统一交易流水号
     * @return 订单信息，不存在返回 null
     */
    TradeResult getOrder(String transNo);
}
