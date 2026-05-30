package com.clmcat.qianyu.mall.rev.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "评价统计查询")
public class ReviewStatQueryDTO {

    @Schema(description = "SPU ID")
    private Long spuId;
}
