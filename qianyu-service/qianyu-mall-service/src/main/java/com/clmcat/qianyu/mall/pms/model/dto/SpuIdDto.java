package com.clmcat.qianyu.mall.pms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "SPU ID 请求")
public class SpuIdDto {

    @Schema(description = "SPU ID")
    private Long spuId;
}
