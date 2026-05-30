package com.clmcat.qianyu.mall.mch.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Table("mch_freight_rule")
public class MerchantFreightRule {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "template_id", comment = "关联运费模板ID")
    private Long templateId;

    @Column(value = "destination_type", comment = "目标地区类型: 1=默认(全国) 2=指定地区")
    private Integer destinationType;

    @Column(value = "destination", comment = "指定地区列表(省份)",
            typeHandler = JacksonTypeHandler.class)
    private List<String> destination;

    @Column(value = "first_unit", comment = "首件/首kg/首m³ 数量")
    private BigDecimal firstUnit;

    @Column(value = "first_price", comment = "首件价格（元）")
    private BigDecimal firstPrice;

    @Column(value = "additional_unit", comment = "续件/续kg/续m³ 数量")
    private BigDecimal additionalUnit;

    @Column(value = "additional_price", comment = "续件价格（元）")
    private BigDecimal additionalPrice;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;
}
