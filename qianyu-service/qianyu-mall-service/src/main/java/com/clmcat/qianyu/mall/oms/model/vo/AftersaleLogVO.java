package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "售后处理日志")
public class AftersaleLogVO {

    @Schema(description = "日志内容")
    private String content;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "操作时间")
    private String createTime;
}
