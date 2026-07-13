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
    private Long startTime;        // 创建时间起（毫秒戳，含）
    private Long endTime;          // 创建时间止（毫秒戳，含）
    private Integer pageNum;
    private Integer pageSize;
}
