package com.clmcat.qianyu.payment.api.trade.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 结算子项（Dubbo RPC 参数）。
 * <p>
 * 单送：1 条，bizNo = transNo；批量送：N 条，各自独立 bizNo。
 *
 * @author ark-home
 * @date 2026-08-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettleItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务流水号（唯一，单次=transNo，批量=独立子编号） */
    private String bizNo;

    /** 收款方用户ID（主播） */
    private Long toUserId;

    /** 结算货币金额（最小单位） */
    private Long settleAmount;

    /** 分佣比例（万分比），如 5000=50% */
    private Integer commissionRate;
}
