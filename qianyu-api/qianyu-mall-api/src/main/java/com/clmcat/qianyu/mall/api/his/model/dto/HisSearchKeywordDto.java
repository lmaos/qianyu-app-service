package com.clmcat.qianyu.mall.api.his.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class HisSearchKeywordDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String keyword;
    private Integer heat;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}
