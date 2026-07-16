package com.clmcat.qianyu.mall.api.cms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 楼层-商品关联 DTO（带 SPU 展示信息，admin 楼层商品页用）。
 */
@Data
public class CmsZoneProductDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long zoneId;
    private Long spuId;
    private Integer sort;
    /** 0=显示 1=隐藏 */
    private Integer status;
    /** 0=手动 1=自动 */
    private Integer source;
    // 关联 SPU 展示信息
    private String spuName;
    private String spuMainImage;
    private Integer spuStatus;
    private String spuPrice;
    private Long spuMerchantId;
}
