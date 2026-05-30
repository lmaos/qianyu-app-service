package com.clmcat.qianyu.mall.api.pay.model.dto;

import lombok.Data;
import java.io.Serializable;

import java.math.BigDecimal;

@Data
public class PayPaymentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String paymentNo;
    private Long orderId;
    private Long userId;
    private Long merchantId;
    private BigDecimal amount;
    private Integer payChannel;
    private Integer payType;
    private Integer payStatus;
    private String transactionId;
    private Integer callbackStatus;
    private Long payTime;
    private Long createTime;
}
