package com.clmcat.qianyu.mall.api.his.model.dto;

import lombok.Data;
import java.io.Serializable;

import java.math.BigDecimal;

@Data
public class HisBrowseHistoryDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long spuId;
    private String spuName;
    private String spuImage;
    private BigDecimal price;
    private Long browseTime;
}
