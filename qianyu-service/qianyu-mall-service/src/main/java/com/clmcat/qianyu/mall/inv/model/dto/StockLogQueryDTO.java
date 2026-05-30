package com.clmcat.qianyu.mall.inv.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "库存日志查询请求")
public class StockLogQueryDTO {

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "SPU ID（查该 SPU 下所有 SKU）")
    private Long spuId;

    @Schema(description = "变动类型：1-下单锁定, 2-支付确认, 3-取消释放, 4-手动增加, 5-手动减少, 6-售后释放")
    private Integer type;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;
}
