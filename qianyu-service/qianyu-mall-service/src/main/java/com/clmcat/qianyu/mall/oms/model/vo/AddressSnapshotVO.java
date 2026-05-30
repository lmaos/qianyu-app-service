package com.clmcat.qianyu.mall.oms.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "收货地址快照")
public class AddressSnapshotVO {

    @Schema(description = "收货人")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区")
    private String district;

    @Schema(description = "详细地址")
    private String detail;
}
