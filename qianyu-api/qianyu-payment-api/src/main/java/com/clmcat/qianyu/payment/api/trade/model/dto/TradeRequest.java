package com.clmcat.qianyu.payment.api.trade.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 交易请求参数（Dubbo RPC）。
 * <p>
 * items 承载结算明细：单送 1 条（bizNo = transNo），批量送 N 条（各自独立 bizNo）。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 付款方用户ID */
    private Long fromUserId;

    /** 虚拟币消费总额（最小单位） */
    private Long coinAmount;

    /** 业务类型（gift/live_room/...） */
    private String bizType;

    /** 业务单号 */
    private String bizId;

    /** 统一交易流水号（由调用方生成，贯穿扣款→订单→结算） */
    private String transNo;

    /** 交易幂等键（全局唯一） */
    private String idempotentKey;

    /** 结算明细列表（不能为空，至少 1 条） */
    private List<SettleItem> items;
}
