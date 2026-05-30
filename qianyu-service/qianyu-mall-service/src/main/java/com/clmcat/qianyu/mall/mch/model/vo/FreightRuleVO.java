package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@Schema(description = "运费规则")
public class FreightRuleVO {

    @Schema(description = "规则 ID")
    private Long id;

    @Schema(description = "目标地区类型：1=默认(全国) 2=指定地区")
    private Integer destinationType;

    @Schema(description = "指定地区列表")
    private List<String> destination;

    @Schema(description = "首件数量")
    private BigDecimal firstUnit;

    @Schema(description = "首件价格（元）")
    private String firstPrice;

    @Schema(description = "续件数量")
    private BigDecimal additionalUnit;

    @Schema(description = "续件价格（元）")
    private String additionalPrice;
}
