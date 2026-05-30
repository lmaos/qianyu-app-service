package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "账单列表查询请求")
public class BillQueryDTO {

    @Schema(description = "账单类型：0-全部, 1-订单收入, 2-退款支出, 3-佣金调整")
    private Integer type;

    @Schema(description = "结算状态：0-未结算, 1-已结算")
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
