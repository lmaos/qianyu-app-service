package com.clmcat.qianyu.mall.log.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "物流详情")
public class LogisticsDetailVO {

    @Schema(description = "物流单 ID")
    private Long logisticsId;

    @Schema(description = "物流公司名称")
    private String logisticsCompany;

    @Schema(description = "物流公司编码")
    private String logisticsCode;

    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "物流状态：1-已揽收, 2-运输中, 3-派送中, 4-已签收, 5-异常")
    private Integer status;

    @Schema(description = "状态中文描述")
    private String statusText;

    @Schema(description = "物流轨迹列表")
    private List<LogisticsTraceVO> traces;
}
