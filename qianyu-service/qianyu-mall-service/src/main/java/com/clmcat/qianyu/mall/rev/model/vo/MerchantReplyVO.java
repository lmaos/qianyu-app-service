package com.clmcat.qianyu.mall.rev.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "商家回复")
public class MerchantReplyVO {

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "回复时间")
    private String createTime;
}
