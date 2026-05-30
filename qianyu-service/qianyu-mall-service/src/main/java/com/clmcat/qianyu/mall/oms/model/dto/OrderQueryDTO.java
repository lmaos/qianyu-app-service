package com.clmcat.qianyu.mall.oms.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "订单列表查询请求")
public class OrderQueryDTO {

    @Schema(description = "订单状态筛选：0=全部, 1=待付款, 2=待发货, 3=待收货, 4=已完成, 5=售后中, 6=已取消/已关闭")
    private Integer status;

    @Schema(description = "订单编号搜索（B端）")
    private String orderSn;

    @Schema(description = "开始时间（B端）")
    private String startTime;

    @Schema(description = "结束时间（B端）")
    private String endTime;

    @Schema(description = "页码，默认 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，默认 10")
    private Integer pageSize = 10;
}
