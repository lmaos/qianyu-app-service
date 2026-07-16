package com.clmcat.qianyu.mall.api.cms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 首页轮播 Banner DTO（admin 查/增/改通用）。 */
@Data
public class CmsBannerDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String description;
    private String actionText;
    private String tagText;
    private String image;
    private String linkUrl;
    /** 链接类型 */
    private Integer linkType;
    private String linkValue;
    private Integer sort;
    /** 0显示 1隐藏 */
    private Integer status;
    private Long createTime;
}
