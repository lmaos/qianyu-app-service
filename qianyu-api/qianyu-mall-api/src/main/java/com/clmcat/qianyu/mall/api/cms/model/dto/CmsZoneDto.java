package com.clmcat.qianyu.mall.api.cms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 楼层（cms_zone）DTO，admin 查/增/改通用。
 */
@Data
public class CmsZoneDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String tagText;
    private String moreText;
    private String layoutMode;
    private Integer productCount;
    private String surfaceBackground;
    private String surfaceShadow;
    private Long categoryId;
    /** 0=仅手动 1=仅自动 2=手动优先+自动补足 */
    private Integer fillMode;
    private Integer sort;
    /** 0=显示 1=隐藏 */
    private Integer status;
    private Long createTime;
}
