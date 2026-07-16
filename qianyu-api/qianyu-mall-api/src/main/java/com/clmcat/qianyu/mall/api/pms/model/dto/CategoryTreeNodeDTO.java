package com.clmcat.qianyu.mall.api.pms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分类树节点（admin 用，含隐藏分类与完整层级字段）。
 */
@Data
public class CategoryTreeNodeDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private String icon;
    private String imgId;
    private Integer sort;
    /** 0=显示 1=隐藏 */
    private Integer status;
    private List<CategoryTreeNodeDTO> children;
}
