package com.clmcat.qianyu.mall.log.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "物流轨迹项")
public class LogisticsTraceDTO {

    @Schema(description = "时间")
    private String time;

    @Schema(description = "轨迹描述")
    private String content;

    @Schema(description = "所在地")
    private String location;
}
