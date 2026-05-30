package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "结算单列表查询请求")
public class SettlementQueryDTO {

    @Schema(description = "结算状态：0-待结算 1-已结算 2-已打款")
    private Integer status;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;
}
