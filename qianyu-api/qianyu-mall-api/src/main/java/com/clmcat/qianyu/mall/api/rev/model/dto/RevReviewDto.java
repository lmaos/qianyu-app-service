package com.clmcat.qianyu.mall.api.rev.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class RevReviewDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long userId;
    private Long spuId;
    private Long skuId;
    private String skuName;
    private Long merchantId;
    private Integer score;
    private String content;
    private Integer isAnonymous;
    private Integer status;
    private Long createTime;
}
