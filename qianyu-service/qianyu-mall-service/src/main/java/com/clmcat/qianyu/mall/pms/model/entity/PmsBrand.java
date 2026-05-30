package com.clmcat.qianyu.mall.pms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("pms_brand")
public class PmsBrand {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "name", comment = "品牌名称")
    private String name;

    @Column(value = "logo", comment = "品牌Logo URL")
    private String logo;

    @Column(value = "description", comment = "品牌描述")
    private String description;

    @Column(value = "sort", comment = "排序值，越小越靠前")
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
