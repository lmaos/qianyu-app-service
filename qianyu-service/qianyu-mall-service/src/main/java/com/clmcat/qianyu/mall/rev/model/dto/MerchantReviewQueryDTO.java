package com.clmcat.qianyu.mall.rev.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商家评价列表查询")
public class MerchantReviewQueryDTO {

    @Schema(description = "SPU ID 筛选")
    private Long spuId;

    @Schema(description = "评分筛选")
    private Integer score;

    @Schema(description = "是否已回复筛选")
    private Boolean hasReply;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;
}
