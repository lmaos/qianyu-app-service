package com.clmcat.qianyu.mall.pms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("pms_category")
public class PmsCategory {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "parent_id", comment = "父分类ID，顶级分类为0")
    private Long parentId;

    @Column(value = "path", comment = "物化路径，从根到当前节点，格式: \"1/5/12\"，顶级为自身ID")
    private String path;

    @Column(value = "img_id", comment = "图片地址")
    private String imgId;

    @Column(value = "name", comment = "分类名称")
    private String name;

    @Column(value = "level", comment = "分类层级: 1=一级, 2=二级, 3=三级...")
    private Integer level;

    @Column(value = "icon", comment = "分类图标URL")
    private String icon;

    @Column(value = "sort", comment = "同级别排序值，越小越靠前")
    private Integer sort;

    @Column(value = "status", comment = "状态: 0=显示, 1=隐藏")
    private Integer status;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除, 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
