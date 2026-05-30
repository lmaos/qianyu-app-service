package com.clmcat.qianyu.mall.api.oms.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OmsOrderDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private BigDecimal couponAmount;
    private BigDecimal discountAmount;
    private Integer totalQuantity;
    private Integer status;
    private Integer afterSaleStatus;
    private Integer afterSaleType;
    private Long version;
    private Integer source;
    private String buyerMessage;
    private Long payTime;
    private Long deliveryTime;
    private Long receiveTime;
    private Long closeTime;
    private Long createTime;
}
