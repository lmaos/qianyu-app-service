package com.clmcat.qianyu.mall.ads.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新收货地址请求")
public class AddressUpdateDTO {

    @Schema(description = "地址 ID")
    private Long addressId;

    @Schema(description = "收货人姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "省编码")
    private String provinceCode;

    @Schema(description = "市编码")
    private String cityCode;

    @Schema(description = "区编码")
    private String districtCode;

    @Schema(description = "详细地址")
    private String detail;

    @Schema(description = "是否默认")
    private Boolean isDefault;

    @Schema(description = "地址标签")
    private String tag;
}
