package com.clmcat.qianyu.mall.mch.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Table("mch_freight_template")
public class MerchantFreightTemplate {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "merchant_id", comment = "商家ID")
    private Long merchantId;

    @Column(value = "name", comment = "模板名称")
    private String name;

    @Column(value = "billing_type", comment = "计价方式: 1=按件数 2=按重量(kg) 3=按体积(m³)")
    private Integer billingType;

    @Column(value = "free_shipping_type", comment = "包邮条件: 0=不包邮 1=满金额包邮 2=满件数包邮")
    private Integer freeShippingType;

    @Column(value = "free_shipping_value", comment = "包邮门槛值（金额或件数）")
    private BigDecimal freeShippingValue;

    @Column(value = "is_default", comment = "是否默认模板: 0=否 1=是")
    private Integer isDefault;

    @Column(value = "status", comment = "状态: 0=禁用 1=启用")
    private Integer status;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
