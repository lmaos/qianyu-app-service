package com.clmcat.qianyu.mall.ads.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "收货地址项")
public class AddressItemVO {

    @Schema(description = "地址 ID")
    private Long id;

    @Schema(description = "收货人姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "国家代码")
    private String country;

    @Schema(description = "省名称")
    private String province;

    @Schema(description = "市名称")
    private String city;

    @Schema(description = "区名称")
    private String district;

    @Schema(description = "详细地址")
    private String detail;

    @Schema(description = "是否默认地址")
    private Boolean isDefault;

    @Schema(description = "完整地址拼接")
    private String fullAddress;
}
