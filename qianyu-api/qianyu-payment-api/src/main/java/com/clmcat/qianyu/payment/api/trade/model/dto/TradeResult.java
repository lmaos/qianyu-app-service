package com.clmcat.qianyu.payment.api.trade.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 交易结果（Dubbo RPC 返回）。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 交易订单ID */
    private Long tradeId;

    /** 统一交易流水号 */
    private String transNo;

    /** 订单状态：0=PENDING 1=SUCCESS 2=CANCELLED 3=REFUNDED */
    private Integer status;
}
