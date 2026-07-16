package com.clmcat.qianyu.mall.backstage.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "SPU 状态流水查询")
public class AdminSpuStatusLogQueryDTO {
    @Schema(description = "SPU ID（精确）")
    private Long spuId;
    @Schema(description = "事件 LIST_ON/LIST_OFF/SUBMIT_AUDIT/AUDIT_PASS/...")
    private String event;
    @Schema(description = "来源 MERCHANT/ADMIN/SYSTEM")
    private String source;
    @Schema(description = "起始时间（毫秒）")
    private Long startTime;
    @Schema(description = "结束时间（毫秒）")
    private Long endTime;
    @Schema(description = "页码")
    private Integer pageNum;
    @Schema(description = "每页条数")
    private Integer pageSize;
}
