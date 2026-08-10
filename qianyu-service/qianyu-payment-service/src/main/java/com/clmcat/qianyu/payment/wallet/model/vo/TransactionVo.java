package com.clmcat.qianyu.payment.wallet.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易流水视图对象（返回前端）。
 *
 * @author ark-home
 * @date 2026-08-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionVo {

    /** 流水ID */
    private Long id;

    /** 统一交易流水号 */
    private String transNo;

    /** 用户ID */
    private Long userId;

    /** 交易类型：1=收入 2=支出 */
    private Integer transType;

    /** 交易金额 */
    private Long amount;

    /** 交易前余额 */
    private Long balanceBefore;

    /** 交易后余额 */
    private Long balanceAfter;

    /** 业务类型 */
    private String bizType;

    /** 业务单号 */
    private String bizId;

    /** 状态：1=成功 2=已回退 */
    private Integer status;

    /** 创建时间戳（毫秒） */
    private Long createTime;
}
