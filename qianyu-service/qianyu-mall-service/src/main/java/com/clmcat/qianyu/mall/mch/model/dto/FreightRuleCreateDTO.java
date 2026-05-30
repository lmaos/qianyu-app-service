package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "运费规则创建项")
public class FreightRuleCreateDTO {

    @Schema(description = "目标地区类型：1=默认(全国) 2=指定地区")
    private Integer destinationType;

    @Schema(description = "指定地区列表（type=2 时必填）")
    private List<String> destination;

    @Schema(description = "首件/首kg/首m³ 数量")
    private BigDecimal firstUnit;

    @Schema(description = "首件价格（元）")
    private String firstPrice;

    @Schema(description = "续件/续kg/续m³ 数量")
    private BigDecimal additionalUnit;

    @Schema(description = "续件价格（元）")
    private String additionalPrice;
}
