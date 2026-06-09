package com.clmcat.qianyu.mall.log.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "物流列表项")
public class LogisticsListItemVO {

    @Schema(description = "物流单 ID")
    private Long id;

    @Schema(description = "订单 ID")
    private Long orderId;

    @Schema(description = "物流单号")
    private String shippingNo;

    @Schema(description = "物流公司名称")
    private String shippingCompanyName;

    @Schema(description = "物流状态：0-已发货, 1-运输中, 2-已签收, 3-异常")
    private Integer status;

    @Schema(description = "状态中文描述")
    private String statusText;

    @Schema(description = "发货时间")
    private String deliveryTime;

    @Schema(description = "签收时间")
    private String receiveTime;
}
