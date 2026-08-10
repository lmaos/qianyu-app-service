package com.clmcat.qianyu.gift.api.gift.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 送礼结果 DTO。
 *
 * @author ark-home
 * @date 2026-08-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiftSendResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 送礼记录ID */
    private Long recordId;

    /** 消费流水号 */
    private String transNo;

    /** 业务流水号（唯一，单次送=trans_no） */
    private String bizNo;

    /** 礼物ID */
    private Long giftId;

    /** 礼物名称 */
    private String giftName;

    /** 实际开出的礼物ID（盲盒场景） */
    private Long actualGiftId;

    /** 实际开出的礼物名称 */
    private String actualGiftName;

    /** 总消费金额 */
    private Long totalAmount;

    /** 主播结算金额 */
    private Long settleAmount;

    /** 订单状态：0=PENDING 1=SUCCESS 2=REFUNDED */
    private Integer status;
}
