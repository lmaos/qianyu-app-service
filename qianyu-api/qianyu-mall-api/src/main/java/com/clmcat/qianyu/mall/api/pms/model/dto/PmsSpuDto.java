package com.clmcat.qianyu.mall.api.pms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PmsSpuDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long merchantId;
    private Long storeId;
    private Long brandId;
    private Long categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private String thumbImage;
    private List<String> images;
    private String description;
    private String keywords;
    private String unit;
    private Integer status;
    private Integer sort;
    private Long freightTemplateId;
    private BigDecimal minPrice;
    private Integer sales;
    private Integer commentCount;
    private BigDecimal avgScore;
    private Long publishTime;
    private Long createTime;
    private Long updateTime;
}
