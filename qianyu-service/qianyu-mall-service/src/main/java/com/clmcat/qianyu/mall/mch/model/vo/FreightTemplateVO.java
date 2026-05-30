package com.clmcat.qianyu.mall.mch.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "运费模板")
public class FreightTemplateVO {

    @Schema(description = "模板 ID")
    private Long id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "计价方式：1=按件数 2=按重量 3=按体积")
    private Integer billingType;

    @Schema(description = "包邮条件：0=不包邮 1=满金额 2=满件数")
    private Integer freeShippingType;

    @Schema(description = "包邮门槛值")
    private String freeShippingValue;

    @Schema(description = "是否默认模板")
    private Boolean isDefault;

    @Schema(description = "状态：0=禁用 1=启用")
    private Integer status;

    @Schema(description = "规则数量")
    private Integer ruleCount;
}
