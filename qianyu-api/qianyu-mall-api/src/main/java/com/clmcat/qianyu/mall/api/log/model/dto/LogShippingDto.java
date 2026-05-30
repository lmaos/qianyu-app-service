package com.clmcat.qianyu.mall.api.log.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class LogShippingDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private String shippingNo;
    private String shippingCompany;
    private String shippingCompanyName;
    private Integer status;
    private Long deliveryTime;
    private Long receiveTime;
    private Long createTime;
    private Long updateTime;
}
