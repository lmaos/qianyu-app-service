package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "更新运费模板请求")
public class FreightTemplateUpdateDTO {

    @Schema(description = "模板 ID")
    private Long templateId;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "计价方式：1/2/3")
    private Integer billingType;

    @Schema(description = "包邮条件：0/1/2")
    private Integer freeShippingType;

    @Schema(description = "包邮门槛值")
    private String freeShippingValue;

    @Schema(description = "是否默认模板")
    private Boolean isDefault;

    @Schema(description = "规则列表")
    private List<FreightRuleCreateDTO> rules;
}
