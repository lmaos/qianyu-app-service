package com.clmcat.qianyu.mall.rev.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商品评价列表查询")
public class ReviewListQueryDTO {

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "评分筛选：0-全部, 1-差评(1-2), 2-中评(3), 3-好评(4-5), 4-有图")
    private Integer score;

    @Schema(description = "排序：createTime / score")
    private String sortField;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;
}
