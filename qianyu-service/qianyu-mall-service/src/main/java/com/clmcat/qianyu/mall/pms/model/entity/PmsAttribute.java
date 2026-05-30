package com.clmcat.qianyu.mall.pms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

@Data
@Table("pms_attribute")
public class PmsAttribute {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "category_id", comment = "所属分类ID")
    private Long categoryId;

    @Column(value = "name", comment = "属性名称（如 color、材质）")
    private String name;

    @Column(value = "type", comment = "属性类型: 1=销售属性, 2=商品参数")
    private Integer type;

    @Column(value = "input_type", comment = "录入方式: 1=手工录入, 2=列表选择")
    private Integer inputType;

    @Column(value = "values", comment = "可选值列表, 格式: [\"红色\",\"蓝色\"]",
            typeHandler = JacksonTypeHandler.class)
    private List<String> values;

    @Column(value = "sort", comment = "排序值，越小越靠前")
    private Integer sort;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除, 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
