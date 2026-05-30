package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "优惠明细项")
public class DiscountItemVO {

    @Schema(description = "优惠类型：promotion/flash_sale/coupon")
    private String type;

    @Schema(description = "对应活动/优惠券 ID")
    private Long id;

    @Schema(description = "优惠名称")
    private String name;

    @Schema(description = "优惠金额（元）")
    private String amount;
}
