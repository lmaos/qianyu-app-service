package com.clmcat.qianyu.mall.mch.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "提现记录查询请求")
public class WithdrawQueryDTO {

    @Schema(description = "提现状态：0=待审核 1=审核通过 2=打款中 3=打款成功 4=审核拒绝 5=打款失败")
    private Integer status;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;
}
