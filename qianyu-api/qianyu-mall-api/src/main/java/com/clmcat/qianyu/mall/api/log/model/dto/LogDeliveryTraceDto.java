package com.clmcat.qianyu.mall.api.log.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class LogDeliveryTraceDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long shippingId;
    private Long traceTime;
    private String description;
    private String location;
    private Integer source;
    private String carrierCode;
    private String rawData;
    private Long createTime;
}
