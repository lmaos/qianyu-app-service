package com.clmcat.qianyu.payment.api.settlement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 结算流水信息（RPC 返回）。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementRecordDto implements Serializable {

    /** 流水ID */
    private Long id;

    /** 消费流水号 */
    private String transNo;

    /** 业务流水号（唯一，单次结算=trans_no） */
    private String bizNo;

    /** 主播用户ID */
    private Long userId;

    /** 类型：1=礼物收益 2=直播收益 3=活动奖励 4=退款 */
    private Integer settleType;

    /** 结算金额 */
    private Long amount;

    /** 结算前余额 */
    private Long balanceBefore;

    /** 结算后余额 */
    private Long balanceAfter;

    /** 当时分佣比例（万分比） */
    private Integer commissionRate;

    /** 状态：1=成功 2=已回退 */
    private Integer status;

    /** 创建时间戳（毫秒） */
    private Long createTime;

    // ===== 常量 =====
    public static final int TYPE_GIFT     = 1;
    public static final int TYPE_LIVE     = 2;
    public static final int TYPE_ACTIVITY = 3;
    public static final int TYPE_REFUND   = 4;

    public static final int STATUS_SUCCESS  = 1;
    public static final int STATUS_REVERSED = 2;
}
