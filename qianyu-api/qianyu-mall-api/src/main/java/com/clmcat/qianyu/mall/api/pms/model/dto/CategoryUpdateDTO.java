package com.clmcat.qianyu.mall.api.pms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新分类请求（admin 用）。
 */
@Data
public class CategoryUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long categoryId;
    private String name;
    private Long parentId;
    private String icon;
    private Integer sort;
    /** 是否启用：true 显示 / false 隐藏 */
    private Boolean enabled;
}
