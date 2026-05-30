package com.clmcat.qianyu.mall.cms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("cms_home_tab")
public class CmsHomeTab {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键")
    private Long id;

    @Column(value = "name", comment = "Tab 显示名称")
    private String name;

    @Column(value = "tab_key", comment = "唯一标识")
    private String tabKey;

    @Column(value = "category_id", comment = "关联分类ID")
    private Long categoryId;

    @Column(value = "icon", comment = "图标URL")
    private String icon;

    @Column(value = "is_default", comment = "是否默认: 0否 1是")
    private Integer isDefault;

    @Column(value = "sort", comment = "排序")
    private Integer sort;

    @Column(value = "status", comment = "0显示 1隐藏")
    private Integer status;

    @Column(value = "create_time")
    private Long createTime;

    @Column(value = "update_time")
    private Long updateTime;

    @Column(value = "deleted", isLogicDelete = true)
    private Integer deleted;
}
