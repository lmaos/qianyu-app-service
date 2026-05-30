package com.clmcat.qianyu.mall.api.ads.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class AdsRegionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private String code;
}
