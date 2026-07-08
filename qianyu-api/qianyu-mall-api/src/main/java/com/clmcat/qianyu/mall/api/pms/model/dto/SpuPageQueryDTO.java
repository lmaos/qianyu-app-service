package com.clmcat.qianyu.mall.api.pms.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class SpuPageQueryDTO implements Serializable {
    private Long merchantId;
    private Long brandId;
    private Long categoryId;
    private Integer status;
    private String keyword;
    private Integer pageNum;
    private Integer pageSize;
}
