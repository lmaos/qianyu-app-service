package com.clmcat.qianyu.mall.api.pms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PmsCategoryDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String path;
    private String imgId;
    private String name;
    private Integer level;
    private String icon;
    private Integer sort;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}
