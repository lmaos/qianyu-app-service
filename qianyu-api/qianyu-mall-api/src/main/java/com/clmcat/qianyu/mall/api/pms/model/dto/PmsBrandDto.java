package com.clmcat.qianyu.mall.api.pms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PmsBrandDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String logo;
    private String description;
    private Integer sort;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}
