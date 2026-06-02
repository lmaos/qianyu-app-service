package com.clmcat.qianyu.mall.rev.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "评价详情页聚合查询")
public class EvaluatePageQueryDTO {

    @Schema(description = "SPU ID（必填）")
    private Long spuId;

    @Schema(description = "评价筛选: 0=全部(默认) / 1=差评(1-2分) / 2=中评(3分) / 3=好评(4-5分) / 4=有图")
    private Integer score;

    @Schema(description = "排序字段: createTime(默认) / score")
    private String sortField;

    @Schema(description = "页码（默认 1）")
    private Integer pageNum;

    @Schema(description = "每页条数（默认 10）")
    private Integer pageSize;
}
