package com.clmcat.qianyu.mall.api.pms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建分类请求（admin 用）。
 */
@Data
public class CategoryCreateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    /** 父分类 ID，不传为一级分类 */
    private Long parentId;
    private String icon;
    private String imgId;
    private Integer sort;
}
