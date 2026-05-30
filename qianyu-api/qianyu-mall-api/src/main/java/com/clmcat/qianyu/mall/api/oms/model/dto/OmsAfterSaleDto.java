package com.clmcat.qianyu.mall.api.oms.model.dto;

import lombok.Data;
import java.io.Serializable;

import java.math.BigDecimal;

@Data
public class OmsAfterSaleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String afterSaleNo;
    private Long orderId;
    private Long orderItemId;
    private Long userId;
    private Long merchantId;
    private Integer type;
    private String reason;
    private String description;
    private BigDecimal amount;
    private String images;
    private Integer status;
    private String rejectReason;
    private String returnShippingNo;
    private String returnShippingCompany;
    private String sendBackShippingNo;
    private String sendBackShippingCompany;
    private Long refundTime;
    private Long createTime;
}
