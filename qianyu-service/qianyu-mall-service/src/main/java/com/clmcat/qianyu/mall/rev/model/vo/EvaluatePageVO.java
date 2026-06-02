package com.clmcat.qianyu.mall.rev.model.vo;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "评价详情页聚合响应")
public class EvaluatePageVO {

    @Schema(description = "SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "商品主图 URL")
    private String spuImage;

    @Schema(description = "评价统计")
    private ReviewStatVO reviewStat;

    @Schema(description = "评价分页列表")
    private Page<ReviewItemVO> reviewList;
}
