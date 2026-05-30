package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "运费模板详情")
public class FreightTemplateDetailVO {

    @Schema(description = "模板 ID")
    private Long id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "计价方式")
    private Integer billingType;

    @Schema(description = "包邮条件")
    private Integer freeShippingType;

    @Schema(description = "包邮门槛值")
    private String freeShippingValue;

    @Schema(description = "是否默认模板")
    private Boolean isDefault;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "规则列表")
    private List<FreightRuleVO> rules;
}
