package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "售后申请响应")
public class AfterSaleCreateVO {

    @Schema(description = "售后单 ID")
    private Long aftersaleId;

    @Schema(description = "售后单号")
    private String aftersaleSn;
}
