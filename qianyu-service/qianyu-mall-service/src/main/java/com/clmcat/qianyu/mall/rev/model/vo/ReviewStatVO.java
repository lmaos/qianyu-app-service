package com.clmcat.qianyu.mall.rev.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "评价统计")
public class ReviewStatVO {

    @Schema(description = "总评价数")
    private Integer totalCount;

    @Schema(description = "好评数")
    private Integer goodCount;

    @Schema(description = "中评数")
    private Integer mediumCount;

    @Schema(description = "差评数")
    private Integer badCount;

    @Schema(description = "有图评价数")
    private Integer hasImageCount;

    @Schema(description = "好评率，如 \"98.50%\"")
    private String goodRate;

    @Schema(description = "平均评分 1.0~5.0")
    private BigDecimal avgScore;
}
