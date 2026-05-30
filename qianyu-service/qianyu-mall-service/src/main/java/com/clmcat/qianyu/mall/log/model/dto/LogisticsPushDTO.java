package com.clmcat.qianyu.mall.log.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "物流公司推送请求")
public class LogisticsPushDTO {

    @Schema(description = "物流公司编码")
    private String logisticsCode;

    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "物流状态")
    private Integer status;

    @Schema(description = "最新轨迹数据")
    private List<LogisticsTraceDTO> traces;

    @Schema(description = "签名")
    private String sign;
}
