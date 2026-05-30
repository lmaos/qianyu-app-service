package com.clmcat.qianyu.mall.rev.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "商家回复评价请求")
public class ReviewReplyDTO {

    @Schema(description = "评价 ID")
    private Long reviewId;

    @Schema(description = "回复内容，最长 200 字符")
    private String content;
}
