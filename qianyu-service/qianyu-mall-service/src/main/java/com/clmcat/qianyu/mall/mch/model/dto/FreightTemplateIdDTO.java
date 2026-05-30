package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "运费模板ID请求")
public class FreightTemplateIdDTO {

    @Schema(description = "模板 ID")
    private Long templateId;
}
