package com.clmcat.qianyu.mall.api.cms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 首页导航 Tab DTO（admin 查/增/改通用）。 */
@Data
public class CmsHomeTabDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String tabKey;
    private Long categoryId;
    private String icon;
    /** 0否 1是 */
    private Integer isDefault;
    private Integer sort;
    /** 0显示 1隐藏 */
    private Integer status;
    private Long createTime;
}
