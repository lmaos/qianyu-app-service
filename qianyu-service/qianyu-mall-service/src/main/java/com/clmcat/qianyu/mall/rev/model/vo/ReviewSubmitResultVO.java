package com.clmcat.qianyu.mall.rev.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "评价提交结果")
public class ReviewSubmitResultVO {

    @Schema(description = "生成的评价 ID 列表")
    private List<Long> reviewIds;
}
