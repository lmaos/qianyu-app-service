package com.clmcat.qianyu.mall.log.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新物流信息请求")
public class LogisticsUpdateDTO {

    @Schema(description = "物流单 ID")
    private Long logisticsId;

    @Schema(description = "物流公司名称")
    private String logisticsCompany;

    @Schema(description = "物流公司编码")
    private String logisticsCode;

    @Schema(description = "物流单号")
    private String logisticsNo;
}
