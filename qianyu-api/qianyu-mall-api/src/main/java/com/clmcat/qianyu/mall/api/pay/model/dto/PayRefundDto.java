package com.clmcat.qianyu.mall.api.pay.model.dto;

import lombok.Data;
import java.io.Serializable;

import java.math.BigDecimal;

@Data
public class PayRefundDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String refundNo;
    private Long paymentId;
    private Long orderId;
    private Long afterSaleId;
    private BigDecimal amount;
    private String reason;
    private Integer refundChannel;
    private Integer refundStatus;
    private String transactionId;
    private Long refundTime;
    private Long createTime;
}
