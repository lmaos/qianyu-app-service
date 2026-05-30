package com.clmcat.qianyu.mall.pms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "评价统计摘要")
public class ReviewStatVo {

    @Schema(description = "总评价数")
    private Integer totalCount;

    @Schema(description = "好评数（score >= 4）")
    private Integer goodCount;

    @Schema(description = "中评数（score = 3）")
    private Integer midCount;

    @Schema(description = "差评数（score <= 2）")
    private Integer badCount;

    @Schema(description = "带图评价数")
    private Integer imageCount;

    @Schema(description = "平均评分")
    private BigDecimal avgScore;

    @Schema(description = "好评率，如 \"98.5%\"")
    private String goodRate;
}
