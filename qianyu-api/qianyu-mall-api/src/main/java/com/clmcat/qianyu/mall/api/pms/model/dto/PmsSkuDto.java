package com.clmcat.qianyu.mall.api.pms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

@Data
public class PmsSkuDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long merchantId;
    private Long spuId;
    private String skuCode;
    private String barcode;
    private List<LinkedHashMap<String, String>> attributes;
    private String skuName;
    private String skuImage;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal costPrice;
    private Integer status;
    private Integer isDefault;
    private BigDecimal weight;
    private BigDecimal volume;
    private Long createTime;
    private Long updateTime;
}
