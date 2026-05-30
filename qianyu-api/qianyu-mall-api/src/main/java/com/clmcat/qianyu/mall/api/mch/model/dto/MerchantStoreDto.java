package com.clmcat.qianyu.mall.api.mch.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class MerchantStoreDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long merchantId;
    private String name;
    private String contactPhone;
    private String logo;
    private String coverImage;
    private String description;
    private Integer status;
    private Long createTime;
    private Long updateTime;
}
