package com.clmcat.qianyu.mall.log.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "物流轨迹")
public class LogisticsTraceVO {

    @Schema(description = "时间")
    private String time;

    @Schema(description = "轨迹描述")
    private String content;

    @Schema(description = "所在地")
    private String location;
}
