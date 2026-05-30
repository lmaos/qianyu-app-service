package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "规格组")
public class SpecGroupVo {

    @Schema(description = "规格名（如\"颜色\"）")
    private String name;

    @Schema(description = "规格值列表（如 [\"红色\",\"蓝色\"]）")
    private List<String> values;
}
