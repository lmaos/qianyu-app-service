package com.clmcat.qianyu.mall.api.oms.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OmsCartDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long merchantId;
    private Long spuId;
    private Long skuId;
    private String skuName;
    private String skuImage;
    private Integer quantity;
    private Integer checked;
    private Long createTime;
    private Long updateTime;
}
