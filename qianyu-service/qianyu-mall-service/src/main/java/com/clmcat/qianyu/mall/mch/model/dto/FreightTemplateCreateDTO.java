package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建运费模板请求")
public class FreightTemplateCreateDTO {

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "计价方式：1=按件数 2=按重量 3=按体积")
    private Integer billingType;

    @Schema(description = "包邮条件：0=不包邮 1=满金额 2=满件数")
    private Integer freeShippingType;

    @Schema(description = "包邮门槛值（元）")
    private String freeShippingValue;

    @Schema(description = "是否默认模板")
    private Boolean isDefault;

    @Schema(description = "规则列表，至少 1 条")
    private List<FreightRuleCreateDTO> rules;
}
